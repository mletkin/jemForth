/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import static io.github.mletkin.jemforth.engine.MemoryMapper.CELL_SIZE;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.exception.IllegalInCompileStateException;

/**
 * Defines the basic {@code jemForth} engine with stack and dictionary.
 *
 * Methods starting with an underscore are implementation of forth words.
 */
public class JemEngine implements Inspectable {

    protected static final char C_QUOTE = '"';
    protected static final char C_SHARP = '#';
    protected static final char C_DOLLAR = '$';
    protected static final char C_PERCENT = '%';
    protected static final char C_TICK = '\'';

    protected static final Integer ZERO = Integer.valueOf(0);
    protected static final Integer ONE = Integer.valueOf(1);
    protected static final Integer MINUS_ONE = Integer.valueOf(-1);

    // values pushed by "find" as result
    protected static final Integer FIND_IMMEDIATE = ONE;
    protected static final Integer FIND_NORMAL = MINUS_ONE;
    protected static final Integer FIND_NOT = ZERO;

    protected static final int INTERPRET = 0;
    protected static final int COMPILE = -1;

    protected static final Integer FALSE = ZERO;
    protected static final Integer TRUE = MINUS_ONE;

    // return stack commands for use as Word definitions

    /**
     * 6.1.0580 &gt;R "R-to" ( x -- ) ( R: -- x )
     *
     * Move x to the return stack.
     */
    protected static final Command<JemEngine> TO_R = c -> c.rStack.push(c.stack.pop());
    protected static final String[] TO_R_C = { "( x -- ) ( R:  -- x )", "Move x from data to return stack." };

    /**
     * 6.1.2060 R&gt; " R-from" ( -- x ) ( R: x -- )
     *
     * Move x from the return stack to the data stack.
     */
    protected static final Command<JemEngine> R_FROM = c -> c.stack.push(c.rStack.pop());
    protected static final String[] R_FROM_C = { "( -- x ) ( R:  x -- )", "Move x from return to data stack." };

    /**
     * 6.1.2070 R@ "R-fetch" ( -- x ) ( R: x -- x )
     *
     * Copy x from the return stack to the data stack.
     */
    protected static final Command<JemEngine> R_FETCH = c -> c.rPeek(1);
    protected static final String[] R_FETCH_C = { "( -- x ) ( R:  x -- x )", "Copy x from return to data stack." };

    /**
     * non-std: drop first element from return stack.
     */
    protected static final Command<JemEngine> R_DROP = c -> c.rStack.pop(); // non standard
    protected static final String[] R_DROP_C = { "( R:  x -- )", "drop top element from data stack." };

    /**
     * non-std: clear domplete return stack.
     */
    protected static final Command<JemEngine> R_CLEAR = c -> c.rStack.clear(); // non standard
    protected static final String[] R_CLEAR_C = { "( R:  x1 .. x2 -- )", "drop all elements from data stack." };

    /**
     * non std: print return stack content.
     */
    protected final static Command<JemEngine> DOT_RSTACK = //
            c -> c.print(((c.rStack.stream().map(c::formatNumber).collect(Collectors.joining(" ")))));
    protected final static String[] DOT_RSTACK_C = { "( -- )", "display the content of the return stack" };

    // Dummy access function for R/O user variables
    protected static final Consumer<Integer> READ_ONLY = v -> {};

    // Execute in the inner interpreter to allow intervention by the debugger
    protected Callback debugCallback = Callback.NOP;

    // hook for string output
    protected Consumer<String> printStr = this::defaultPrint;

    // hook for character output
    protected Consumer<Character> printChar = this::defaultPrint;

    // The Forth dictionary
    protected Dictionary dictionary = new Dictionary();

    // The data stack
    protected IntegerStack stack = new IntegerStack();

    // push a key from terminal input to the stack
    protected Supplier<Character> readChar = () -> (char) -1;

    // push a key from terminal input to the stack
    protected Supplier<Boolean> isCharAvailable = () -> false;

    /**
     * The return stack.
     * <p>
     * The return stack is used by the engine to keep track of the return addresses when words (aka sub routines) are
     * called. The return stack may be used by the forth system but the access is restricted to specified JemEngine
     * methods. The impleentation might vary.
     */
    private ReturnStack rStack = new ReturnStack();

    // Entry point for the debuggung tools
    protected Inspector inspector = new Inspector(this);

    // number base for number conversion, by convention hex during engine boot
    protected int base = 16;

    // state is either compile or interpret
    protected int state = INTERPRET;

    // The terminal input buffer.
    public StringWord tibWord = new StringWord("TIB");

    // The current position in the tib
    protected int toIn = 0;

    // The word buffer for the input parser (the name is not used anywhere).
    public StringWord wordBuffer = new StringWord("wordBuffer");

    // The instruction pointer for the inner interpreter loop
    protected int ip;

    {
        // The Dictionary needs a default vocabulary, this is always the "FORTH" dictionary
        add(dictionary.getSearchResolver().createVocabulary("FORTH"));

        // internal variables are accessible through FORTH words
        add(new UserVariableWord("BASE", () -> base, v -> base = v)); // 6.1.0750
        add(new UserVariableWord("STATE", () -> state, READ_ONLY)); // 6.1.2250
        add(new UserVariableWord(">IN", () -> toIn, v -> toIn = v)); // 6.1.0560
        add(new UserVariableWord("IP", () -> ip, READ_ONLY));
        add(wordBuffer.comment("word input buffer"));
        add(tibWord.comment("terminal input buffer"));
        add(new UserVariableWord("#TIB", () -> tibWord.length(), READ_ONLY)
                .comment("number of bytes in the terminal input buffer"));

        // (STRLITERAL) is used by the inspector to identify the string coming after.
        add(STRING_LITERAL, JemEngine::lit);
    }

    // some internal words we need to reference in the engine
    protected Word exitWord = add("EXIT", JemEngine::_exit);
    protected Word litWord = add("(LITERAL)", JemEngine::lit);
    protected Word branchWord = add("BRANCH", JemEngine::_branch);
    protected Word zeroBranchWord = add("?BRANCH", JemEngine::_0branch);
    protected Word doesToWord = add("DOES>", JemEngine::_doesTo).immediate();

    @Override
    public void reset(boolean executionOnly) {
        ip = 0;
        rStack.clear();
        state = INTERPRET;
        if (!executionOnly) {
            stack.clear();
            tibWord.clear();
            wordBuffer.clear();
            toIn = 0;
        }
    }

    /**
     * 6.1.2450 WORD ( char "&lt;chars&gt;ccc&lt;char&gt;" -- c-addr ).
     * <p>
     * Skip leading delimiters. Parse characters ccc delimited by char.<br>
     * c-addr is the address of a transient region containing the parsed word as a counted string. If the parse area was
     * empty or contained no characters other than the delimiter, the resulting string has a zero length. A program may
     * replace characters within the string.
     */
    protected void _word() {
        int delimiter = stack.iPop();
        CheckChar check = delimiter == 32 ? Character::isWhitespace : c -> c == (char) (delimiter & 0xFF);
        wordBuffer.data(parse(check));
        stack.push(wordBuffer.xt + 1);
    }

    /**
     * 6.1.1550 FIND ( c-addr -- c-addr 0 | xt 1 | xt -1 ).
     * <p>
     * F83 F2012<br>
     * Find the definition named in the counted string at c-addr.<br>
     * If the definition is not found, return c-addr and zero.<br>
     * If the definition is found, return its execution token xt.<br>
     * If the definition is immediate, also return one (1),<br>
     * otherwise also return minus-one (-1).<br>
     * The values returned by FIND while compiling may differ from those returned while not compiling.
     */
    protected void _find() {
        Word word = find(dictionary.findString(stack.peek()));
        if (word == null) {
            stack.push(FIND_NOT);
        } else {
            stack.pop();
            stack.push(word.xt);
            stack.push(word.isImmediate() ? FIND_IMMEDIATE : FIND_NORMAL);
        }
    }

    /**
     * Definition for the parse delimiter predicate.
     */
    @FunctionalInterface
    protected interface CheckChar {
        boolean isDelimiter(char c);
    }

    /**
     * Skip white space and read next word from tib delimited by white space.
     * <p>
     * Acts like figForth ENCLOSE<br>
     * see 6.2.2020 PARSE-NAME
     *
     * @return the parsed name
     */
    protected String parseName() {
        return parse(Character::isWhitespace);
    }

    /**
     * Parses a word delimited by check in the tib.
     * <p>
     *
     * see 6.2.2008 PARSE
     *
     * @param check
     *            An expression to identify the delimiter
     * @return the word parsed
     */
    protected String parse(CheckChar check) {
        StringBuffer result = new StringBuffer("");

        // skip delimiter
        while (toIn < tibWord.length() && Character.isWhitespace(tibWord.charAt(toIn))) {
            toIn++;
        }

        // read to the next delimiter
        while (toIn < tibWord.length() && !check.isDelimiter(tibWord.charAt(toIn))) {
            result.append(tibWord.charAt(toIn));
            toIn++;
        }
        toIn++;
        return result.toString();
    }

    /**
     * Checks the compile state.
     *
     * @return {@code true} iff the engine is in compile state
     */
    protected boolean isStateCompile() {
        return state != INTERPRET;
    }

    protected void assertCompileState() {
        if (!isStateCompile()) {
            throw new IllegalInCompileStateException();
        }
    }

    protected void assertInterpretationState() {
        if (isStateCompile()) {
            throw new IllegalInCompileStateException();
        }
    }

    /**
     * Compiles the given value to the current word in the dictionary.
     * <p>
     * see 6.1.0150 "comma"
     *
     * @param number
     *            the value to compile
     */
    public void comma(Integer number) {
        dictionary.getCurrentWord().addPfaEntry(number);
    }

    /**
     * 6.1.0450 starts the definition of a colon word.
     */
    protected void _colon() {
        assertInterpretationState();
        dictionary.create(new ColonWord(), parseName());
        state = COMPILE;
    }

    /**
     * 6.1.0460 closes the definition of the current dictionary entry.
     */
    protected void _semicolon() {
        assertCompileState();
        comma(exitWord.xt);
        state = INTERPRET;
    }

    /**
     * Look for the word with the given name in the dictionary.
     *
     * @param wordName
     *            name of the word to find
     * @return the word found or {@code null}
     */
    protected Word find(String wordName) {
        return dictionary.find(wordName);
    }

    /**
     * Convert the token string to a number.
     * <ul>
     * <li>2012 std: recognize dec, bin, hex and char ignoring the BASE.
     * <li>containing "." means double precision
     * </ul>
     *
     * @param token
     *            token to convert
     * @return converted token
     */
    protected Number toLiteral(String token) {
        if (Util.isEmpty(token)) {
            return null;
        }
        BiFunction<String, Integer, Number> convert = token.indexOf('.') > 0 ? Long::parseLong : Integer::parseInt;
        String plainToken = token.replace(".", "");
        try {
            switch (plainToken.charAt(0)) {
            case C_SHARP:
                return convert.apply(plainToken.substring(1), 10);
            case C_DOLLAR:
                return convert.apply(plainToken.substring(1), 16);
            case C_PERCENT:
                return convert.apply(plainToken.substring(1), 2);
            case C_TICK:
                if (token.length() == 3 && token.charAt(2) == C_TICK) {
                    return (int) token.charAt(1);
                }
            default:
                return convert.apply(plainToken, base);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Output Functions

    @Override
    public void setStringPrinter(Consumer<String> printer) {
        printStr = printer;
    }

    @Override
    public void setCharPrinter(Consumer<Character> printer) {
        printChar = printer;
    }

    @Override
    public void setReadChar(Supplier<Character> lambda) {
        readChar = lambda;
    }

    @Override
    public void setIsCharAvailable(Supplier<Boolean> lambda) {
        isCharAvailable = lambda;
    }

    @Override
    public void print(String str) {
        printStr.accept(str);
    }

    private void defaultPrint(String str) {
        try {
            System.err.write(str.getBytes());
            System.err.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void defaultPrint(Character zch) {
        if (zch != null) {
            System.err.write(zch.charValue());
            System.err.flush();
        }
    }

    protected void printReturnStack() {
        rStack.stream()//
                .map(v -> v == null ? "null" : formatNumber(v)) //
                .map(i -> (i != null ? i.toString() : "null") + " ")//
                .forEach(this::print);
    }

    // Inner Interpreter functions

    /**
     * Executes a word.
     * <p>
     * Usually called once to start the outer interpreter loop.<br>
     * May also be called to execute a word directly.
     *
     * @param word
     *            {@link Word}-Instance to execute
     */
    public void execute(Word word) {
        word.execute(this);
        while (ip != 0) {
            _next();
        }
    }

    /**
     * Executes a whole -- or part of a -- word list of a cell list definition.
     * <p>
     * Used with a colon definition or the DOES&gt; part of a word definition.
     *
     * @param pfa
     *            absolute address of a cell in a cell list word
     */
    public void docol(int pfa) {
        rStack.push(ip);
        ip = pfa;
    }

    /**
     * Executes a command and advances the ip.
     */
    public void _next() {
        int currentPosition = ip;
        ip = ip + CELL_SIZE;
        dictionary.fetchWord(currentPosition).execute(this);
        debugCallback.call(this);
    }

    /**
     * Return from a subroutine.
     */
    protected void _exit() {
        ip = rStack.pop();
    }

    // compile functions

    /**
     * The (xt of the) following word is compiled to the dictionary.
     * <p>
     * The legacy way of compiling non-immediate words (COMPILE).<br>
     * replaced by POSTPONE in the 2012 standard.
     */
    protected void _compile() {
        comma(dictionary.fetchWord(ip).xt);
        ip = ip + CELL_SIZE;
    }

    /**
     * 6.1.1780 LITERAL.
     * <p>
     * The runtime behavior of the LITERAL word:<br>
     * push content from the memory cell referenced by the ip to the stack and advance
     */
    protected void lit() {
        stack.push(dictionary.fetch(ip));
        ip = ip + CELL_SIZE;
    }

    /**
     * Performs an unconditional branch with an absolute address.
     */
    protected void _branch() {
        ip = dictionary.fetch(ip);
    }

    /**
     * Performs a conditional branch on zero with an absolute address.
     */
    protected void _0branch() {
        if (stack.iPop() == 0) {
            _branch();
        } else {
            ip = ip + CELL_SIZE;
        }
    }

    /**
     * 6.1.2520 [CHAR]
     * <p>
     * Compilation ( "&lt;spaces&gt;name" -- )<br>
     * Skip leading space delimiters. Parse name delimited by a space. Append the run-time semantics given below to the
     * current definition. Parse the next word and compile the first character's code as literal.
     * <p>
     * Run-time: ( -- char )<br>
     * Place char, the value of the first character of name, on the stack.
     */
    protected void _bracketChar() {
        String name = parseName();
        if (!Util.isEmpty(name)) {
            comma(litWord.xt);
            comma((int) name.charAt(0));
        }
    }

    /**
     * cfa lambda for DOES&gt; runtime section.
     *
     * @param adrToPush
     *            address to push on the stack
     * @param adrToRun
     *            address to execute
     */
    public void doDoesTo(int adrToPush, int adrToRun) {
        stack.push(adrToPush);
        docol(adrToRun);
    }

    /**
     * 6.1.1250 DOES&gt;.
     *
     * FIXME: Check, this might be incomplete
     */
    protected void _doesTo() {
        if (isStateCompile()) {
            // compile means compilation of defining word containing the DOES> section
            comma(doesToWord.xt);
            comma(exitWord.xt);
        } else {
            // interpretation means execution of defining word
            // must push address of defined word when later executed...
            Word word = dictionary.getCurrentWord();
            int runtimeAddress = ip + CELL_SIZE; // jump over exit
            int pfaOfCurrentWord = word.xt + CELL_SIZE;
            word.cfa = c -> c.doDoesTo(pfaOfCurrentWord, runtimeAddress);
            comma(exitWord.xt);
        }
    }

    /**
     * &gt;LITERAL non-standard.
     * <p>
     * ( addr -- addr 0 )<br>
     * ( addr -- n 1 )<br>
     * Take the string addressed by the top of the stack and tries to convert the string to a literal.
     */
    protected void toLiteral() {
        Integer numString = stack.pop();
        Number number = toLiteral(dictionary.findString(numString));
        if (number == null) {
            stack.push(numString);
            stack.push(ZERO);
        } else {
            stack.push(number);
            stack.push(ONE);
        }
    }

    /**
     * Pushes the nth element from the return stack to the data stack.
     *
     * @param n
     *            the index of the element starting an 0 (top of stack)
     */
    public void rPeek(int n) {
        stack.push(rStack.peek(n));
    }

    /**
     * 6.1.2120 RECURSE ( -- ) compile only.
     * <p>
     * Append the execution semantics of the current definition to the current definition.
     */
    protected void _recurse() {
        assertCompileState();
        comma(dictionary.getCurrentWord().xt);
    }

    // Inspectable implementation, not Forth: Access for IDE

    @Override
    public IntegerStack getDataStack() {
        return stack;
    }

    @Override
    public ReturnStack getReturnStack() {
        return rStack;
    }

    @Override
    public Stream<Integer> getReturnStackContent() {
        return rStack.stream();
    }

    @Override
    public Dictionary getDictionary() {
        return dictionary;
    }

    @Override
    public void setDebugCallback(Callback callBack) {
        debugCallback = callBack == null ? Callback.NOP : callBack;
    }

    @Override
    public Callback getDebugCallback() {
        return debugCallback;
    }

    @Override
    public int getIp() {
        return ip;
    }

    @Override
    public Inspector getInspector() {
        return inspector;
    }

    @Override
    public String formatNumber(long number) {
        return Long.toString(number, base).toUpperCase();
    }

    @Override
    public int getBase() {
        return base;
    }

    @Override
    public int getState() {
        return state;
    }

    /**
     * Add a complete word to the dictionary.
     *
     * @param word
     *            the word to add
     * @return the added {@link Word}-Object
     */
    public Word add(Word word) {
        dictionary.add(word);
        return word;
    }

    /**
     * Compile and add a FORTH written (colon) definition to the dictionary.
     *
     * @param definition
     *            the forth definition to compile
     * @return the added {@link Word}-Object
     */
    public Word add(String definition) {
        process(definition);
        return dictionary.getCurrentWord();
    }

    /**
     * Creates a new internal word and adds it to the dictionary.
     * <p>
     * Subclasses of the engine might have to redefine the {@code add} method to add methods from the subclass as
     * command.
     *
     * @param <T>
     *            Type of the engine to which the command applies
     * @param name
     *            name of the word
     * @param command
     *            command to execute
     * @return the created and added {@link Word}-Object
     */
    public <T extends JemEngine> Word add(String name, Command<T> command) {
        return add(new InternalWord(name, command));
    }
}
