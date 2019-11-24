/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import static io.github.mletkin.jemforth.engine.MemoryMapper.CELL_SIZE;
import static io.github.mletkin.jemforth.engine.Util.not;
import static java.util.Optional.ofNullable;

import java.util.stream.Collectors;

import io.github.mletkin.jemforth.engine.exception.ForthTerminatedException;
import io.github.mletkin.jemforth.engine.exception.IllegalMemoryAccessException;

/**
 * Contains word definitions that are common to all engines.
 *
 * Functions that are essential for the engine should be located in the {@code JemEngine}.
 */
public class ForthEngine extends JemEngine {

    /**
     * 6.1.1660 HEX ( -- )
     *
     * Set contents of BASE to sixteen.
     */
    protected final static Command<ForthEngine> BASE_HEX = c -> c.base = 16;
    protected final static String[] BASE_HEX_C = { "( -- )", "set base to 16" };

    /**
     * 6.1.1170 DECIMAL ( -- )
     *
     * Set contents of BASE to ten.
     */
    protected final static Command<ForthEngine> BASE_DECIMAL = c -> c.base = 10;
    protected final static String[] BASE_DECIMAL_C = { "( -- )", "set base to 10" };

    /**
     * no-std OCTAL ( -- )
     *
     * Set contents of BASE to 8.
     */
    protected final static Command<ForthEngine> BASE_OCTAL = c -> c.base = 8;
    protected final static String[] BASE_OCTAL_C = { "( -- )", "set base to 8" };

    /**
     * no-std BIN ( -- )
     *
     * Set contents of BASE to 2.
     */
    protected final static Command<ForthEngine> BASE_BIN = c -> c.base = 2;
    protected final static String[] BASE_BIN_C = { "( -- )", "set base to 2" };

    /**
     * 6.1.0720 AND ( x1 x2 -- x3 )
     *
     * x3 is the bit-by-bit logical "and" of x1 with x2.
     */
    protected final static Command<ForthEngine> BIT_AND = c -> c.stack.push(c.stack.iPop() & c.stack.iPop());
    protected final static String[] BIT_AND_C = { "( n1 n2 -- n3 )", "compute bitwise and" };

    /**
     * 6.1.1980 OR ( x1 x2 -- x3 )
     *
     * x3 is the bit-by-bit inclusive-or of x1 with x2.
     */
    protected final static Command<ForthEngine> BIT_OR = c -> c.stack.push(c.stack.iPop() | c.stack.iPop());
    protected final static String[] BIT_OR_C = { "( n1 n2 -- n3 )", "compute bitwise inclusive or" };

    /**
     * 6.1.2490 XOR ( x1 x2 -- x3 )
     *
     * x3 is the bit-by-bit exclusive-or of x1 with x2.
     */
    protected final static Command<ForthEngine> BIT_XOR = c -> c.stack.push(c.stack.iPop() ^ c.stack.iPop());
    protected final static String[] BIT_XOR_C = { "( n1 n2 -- n3 )", "compute bitwise exclusive or" };

    /**
     * 6.1.1720 INVERT ( x1 -- x2 )
     *
     * Invert all bits of x1, giving its logical inverse x2.
     */
    protected final static Command<ForthEngine> BIT_INVERT = c -> c.stack.push(~c.stack.iPop());
    protected final static String[] BIT_INVERT_C = { "( n1 -- n2 )", "invert all bits -- aka one's complement" };

    /**
     * 6.1.0530 = "equal" ( x1 x2 -- flag )
     *
     * flag is true if and only if x1 is bit-for-bit the same as x2.
     */
    protected final static Command<ForthEngine> CMP_EQ = c -> c.stack.push(c.stack.iPop() == c.stack.iPop());
    protected final static String[] CMP_EQ_C = { "( n1 n2 -- flag )", "true if n1 equals n2" };

    /**
     * 6.1.0540 &gt; "more" ( n1 n2 -- flag )
     *
     * flag is true if and only if n1 is greater than n2.
     */
    protected final static Command<ForthEngine> CMP_GT = c -> c.stack.push(c.stack.iPop() < c.stack.iPop());
    protected final static String[] CMP_GT_C = { "( n1 n2 -- flag )", "true if n1 is greater than n2" };

    /**
     * 6.1.0480 &lt; "less" ( n1 n2 -- flag )
     *
     * flag is true if and only if n1 is less than n2.
     */
    protected final static Command<ForthEngine> CMP_LT = c -> c.stack.push(c.stack.iPop() > c.stack.iPop());
    protected final static String[] CMP_LT_C = { "( n1 n2 -- flag )", "true if n1 is less than n2" };

    /**
     * 6.2.0500 &lt;&gt; "not-equal"
     */
    protected final static Command<ForthEngine> CMP_NE = c -> c.stack.push(c.stack.iPop() != c.stack.iPop());
    protected final static String[] CMP_NE_C = { "( n1 n2 -- flag )", "true if n1 not equals n2" };

    /**
     * 6.1.0270 0= "zero-equal" ( x -- flag )
     *
     * flag is true if and only if x is equal to zero.
     */
    protected final static Command<ForthEngine> CMP_0_EQ = c -> c.stack.push(c.stack.iPop() == 0);
    protected final static String[] CMP_0_EQ_C = { "( n -- flag )", "true if n equals zero" };

    /**
     * 6.2.0280 0&gt; "zero-more" ( n -- flag )
     *
     * flag is true if and only if n is greater than zero.
     */
    protected final static Command<ForthEngine> CMP_0_GT = c -> c.stack.push(c.stack.iPop() > 0);
    protected final static String[] CMP_0_GT_C = { "( n -- flag )", "true is n is greater than zero" };

    /**
     * 6.1.0250 0&lt; "zero-less" ( n -- flag )
     *
     * flag is true if and only if n is less than zero.
     */
    protected final static Command<ForthEngine> CMP_0_LT = c -> c.stack.push(c.stack.iPop() < 0);
    protected final static String[] CMP_0_LT_C = { "( n -- flag )", "true if n is less than zero" };

    /**
     * 6.2.0260 0&lt;&gt; "zero-not-equal" ( x -- flag )
     *
     * flag is true if and only if x is not equal to zero.
     */
    protected final static Command<ForthEngine> CMP_0_NE = c -> c.stack.push(c.stack.iPop() != 0);
    protected final static String[] CMP_0_NE_C = { "( n -- flag )", "true if n not equals zero" };

    /**
     * 15.6.2.0830 BYE ( -- )
     *
     * Return control to the host operating system, if any.
     */
    protected final static Command<ForthEngine> BYE = ForthEngine::_bye;
    protected final static String[] BYE_C = { "( -- )", "terminate excution" };

    /**
     * 6.1.0550 &gt;BODY ( xt -- a-addr ).
     *
     * a-addr is the data-field address corresponding to xt.
     */
    protected final static Command<ForthEngine> TO_BODY = ForthEngine::_toBody;
    protected final static String[] TO_BODY_C = { "( xt -- a-addr )",
            "a-addr is the data-field address corresponding to xt." };

    /**
     * 6.1.2450 WORD ( char "&lt;chars&gt;ccc&lt;char&gt;" -- c-addr ).
     *
     * Copy the next word delimited by the character on the stack to the word buffer.<br>
     * Push the address of the length byte of the word buffer
     */
    protected final static Command<JemEngine> WORD = JemEngine::_word;
    protected final static String[] WORD_C = { "( char <name> -- c-addr )",
            "copy the name delimited by char to the wordbuffer and push it's length byte address" };

    /**
     * 6.1.1550 FIND ( c-addr -- c-addr 0 | xt 1 | xt -1 ).
     *
     * F83 F2012<br>
     * Find the definition named in the counted string at c-addr.<br>
     * If the definition is not found, return c-addr and zero.<br>
     * If the definition is found, return its execution token xt.<br>
     * If the definition is immediate, also return one (1),<br>
     * otherwise also return minus-one (-1).<br>
     * The values returned by FIND while compiling may differ from those returned while not compiling.
     */
    protected final static Command<JemEngine> FIND = JemEngine::_find;
    protected final static String[] FIND_C = { "( c-addr -- c-addr 0 | xt 1 | xt -1 )",
            "find a word and push the result of the search" };

    /**
     * 6.1.0450 : "colon" ( C: "&lt;spaces&gt;name" -- colon-sys ).
     *
     * Skip leading space delimiters. Parse name delimited by a space. Create a definition for name, called a "colon
     * definition". Enter compilation state and start the current definition, producing colon-sys. Append the initiation
     * semantics given below to the current definition.
     */
    protected final static Command<JemEngine> COLON = JemEngine::_colon;
    protected final static String[] COLON_C = { "( <name> -- )", "start the definition of a colon word" };

    /**
     * 6.1.0460 ; "semicolon" ( C: colon-sys -- ).
     *
     * Close definition of the current dictionary entry, align space
     */
    protected final static Command<JemEngine> SEMICOLON = JemEngine::_semicolon;
    protected final static String[] SEMICOLON_C = { "( -- )", "end the definition of a colon word" };

    /**
     * 6.1.1650 HERE ( -- addr )
     *
     * addr is the data-space pointer.
     */
    protected final static Command<JemEngine> HERE = c -> c.dictionary.getHereValue();
    protected final static String[] HERE_C = { "( -- addr )", "pushes the data space pointer" };

    /**
     * 15.6.2.1580 FORGET ( "&lt;spaces&gt;name" -- ).
     *
     * Forget all words following (and including) the given word.
     */
    protected final static Command<JemEngine> FORGET = c -> ofNullable(c.find(c.parseName()))
            .ifPresent(c.dictionary::forget);
    protected final static String[] FORGET_C = { "(<spaces>name -- )",
            "Forget all words following (and including) the given word." };

    /**
     * 6.1.1710 IMMEDIATE ( -- ).
     *
     * Set the immediate word of the word currently in compilation.
     */
    protected final static Command<JemEngine> IMMEDIATE = c -> c.dictionary.getCurrentWord().immediate();
    protected final static String[] IMMEDIATE_C = { "( -- )",
            "Set the immediate flag of the word currently in compilation." };

    /**
     * 6.1.1000 CREATE ( "&lt;spaces&gt;name" -- ).
     *
     * Execution semantics: ( -- a-addr ) push the nanmes Data field<br>
     * this is achieved through the default behavior of "Word.cfa"<br>
     * aligns memory
     */
    protected final static Command<JemEngine> CREATE = c -> c.dictionary.create(new CellListWord(), c.parseName());
    protected final static String[] CREATE_C = { "( <name> -- )", "creates a new word with the given name" };

    /**
     * 6.2.0825 BUFFER: (buffer-colon) ( u "&lt;spaces&gt;name" -- ).
     *
     * Skip leading space delimiters. Parse name delimited by a space. Create a definition for name, with the execution
     * semantics defined below. Reserve u address units at an aligned address.
     */
    protected final static Command<JemEngine> BUFFER_COLON = Command.NOP;
    protected final static String[] BUFFER_COLON_C = { "( u \"<name>\" -- ; -- addr )",
            "create a buffer of aligned uninitialized space" }; //

    /**
     * 6.1.0710 ALLOT ( n -- ).
     *
     * If n is greater than zero, reserve n address units of data space. If n is less than zero, release | n | address
     * units of data space. If n is zero, leave the data-space pointer unchanged.
     *
     * FIXME: memory release is not impllemented
     */
    protected final static Command<JemEngine> ALLOT = c -> c.dictionary.allot(c.stack.iPop());
    protected final static String[] ALLOT_C = { "( n -- )", "allocate n bytes of memory" };

    /**
     * 6.1.0950 CONSTANT ( x "&lt;spaces&gt;name" -- ).
     *
     * Create a constant in the dictionary.
     */
    protected final static Command<JemEngine> CONSTANT = //
            c -> c.dictionary.create(new ConstantWord(c.stack.pop()), c.parseName());
    protected final static String[] CONSTANT_C = { "( x <name> -- )",
            "creates a constant with the given name and the tos value as value." };

    /**
     * 6.1.2410 VARIABLE ( "&lt;spaces&gt;name" -- ).
     *
     * Create a variable in the dictionary.
     */
    protected final static Command<JemEngine> VARIABLE = c -> c.dictionary.create(new VariableWord(), c.parseName());
    protected final static String[] VARIABLE_C = { "( <name> -- )", "creates a variable with the given name." };

    /**
     * 6.1.0070 ' ( &lt;name&gt; -- xt ).
     *
     * Skip leading space delimiters. Parse name delimited by a space. Find name and return xt, the execution token for
     * name.<br>
     * An ambiguous condition exists if name is not found.<br>
     * this implementation is not state aware, use ['] instead
     */
    protected final static Command<JemEngine> TICK = c -> c.stack.push(c.find(c.parseName()).xt);
    protected final static String[] TICK_C = { "( <name> -- xt )", "Find a wod and push its xt on the stack" };

    /**
     * 6.1.1370 EXECUTE ( xt -- ).
     *
     * Remove xt from the stack and perform the semantics identified by it.
     */
    protected final static Command<JemEngine> EXECUTE = //
            c -> c.dictionary.findWordContainingPfa(c.stack.pop()).execute(c);
    protected final static String[] EXECUTE_C = { "( xt -- )", "execute the word whose xt is on the stack" };

    /**
     * 6.1.0150 , comma ( x -- ).
     *
     * Reserve one cell of data space and store x in the cell. If the data-space pointer is aligned when , begins
     * execution, it will remain aligned when , finishes execution. An ambiguous condition exists if the data-space
     * pointer is not aligned prior to execution of ,
     */
    protected final static Command<JemEngine> COMMA = c -> c.comma(c.stack.pop());
    protected final static String[] COMMA_C = { "( x -- )", "allots a cell and stores the tos value there" };

    /**
     * 6.2.0945 COMPILE, ( xt -- ).
     *
     * Compile the word specified by the address on the stack.<br>
     * Tis is the portable alternative to the comma word.<br>
     * This implementation makes no distinction.
     **/
    protected final static Command<JemEngine> COMPILE_COMMA = COMMA;
    protected final static String[] COMPILE_COMMA_C = { "( x -- )", "alias for the \"comma\" word" };

    /**
     * 6.1.0860 C, "C comma" ( char -- ).
     *
     * Reserve space for one character in the data space and store char in the space. If the data-space pointer is
     * character aligned when C, begins execution, it will remain character aligned when C, finishes execution. An
     * ambiguous condition exists if the data-space pointer is not character-aligned prior to execution of C,.
     */
    protected final static Command<JemEngine> C_COMMA = c -> c.dictionary.addByte(c.stack.pop());
    protected final static String[] C_COMMA_C = { "( x -- )", "allots a byte and stores the tos value there" };

    /**
     * 6.1.2033 POSTPONE ( "&lt;spaces&gt;name" -- ).
     *
     * compilation<br>
     * Skip leading space delimiters. Parse name delimited by a space. Find name. Append the compilation semantics of
     * name to the current definition. An ambiguous condition exists if name is not found.<br>
     * replaces [COMPILE] to compile immediate words
     */
    protected final static Command<JemEngine> POSTPONE = c -> c.comma(c.dictionary.find(c.parseName()).xt);
    protected final static String[] POSTPONE_C = { "(<name> -- )",
            "find a word and compile its xt to the current word" };

    /**
     * 6.1.2120 RECURSE ( -- ).
     *
     * Append the execution semantics of the current definition to the current definition. An ambiguous condition exists
     * if RECURSE appears in a definition after DOES&gt;.<br>
     * FIXME: prohibit use of curret word
     */
    protected final static Command<JemEngine> RECURSE = JemEngine::_recurse;
    protected final static String[] RECURSE_C = { "(<name> -- )", "call the current word in it's own definition" };

    /**
     * 6.1.2500 [ "bracket" ( -- ) IMMEDIATE.
     *
     * enter interpretation state.
     */
    protected final static Command<ForthEngine> BRACKET = c -> c.state = JemEngine.INTERPRET;
    protected final static String[] BRACKET_C = { "( -- )", "enter interpretation state" };

    /**
     * 6.1.2540 ] "right-bracket" ( -- ).
     *
     * enter compilation state.
     */
    protected final static Command<ForthEngine> RIGHT_BRACKET = c -> c.state = JemEngine.COMPILE;
    protected final static String[] RIGHT_BRACKET_C = { "( -- )", "enter compilation state" };

    /**
     * 6.1.2510 ['] ( "&lt;spaces&gt;name" -- ) compilation only
     *
     * Skip leading space delimiters. Parse name delimited by a space. Find name. compile xt as literal, so it will be
     * pushed on stack when executed.
     */
    protected final static Command<ForthEngine> BRACKET_TICK_BRACKET = ForthEngine::_bracketTickBracket;
    protected final static String[] BRACKET_TICK_BRACKET_C = { "(<name> -- )",
            "find a word and compile its xt as literal" };

    /**
     * 6.1.2520 [CHAR] ( "&lt;spaces&gt;name" -- char ).
     *
     * Parse the next word and compile the first character's code as literal.
     */
    protected final static Command<JemEngine> BRACKET_CHAR = JemEngine::_bracketChar;
    protected final static String[] BRACKET_CHAR_C = { "(<name> -- )",
            "compile the first char of the next word as literal" };

    /**
     * 6.1.1750 KEY ( -- char ).
     *
     * Receive one character and put it on the stack.
     */
    protected final static Command<JemEngine> KEY = c -> c.stack.cPush(c.readChar.get().charValue());
    protected final static String[] KEY_C = { "( -- char )", "Receive one character and put it on the stack." };

    /**
     * 6.1.1755 KEY? ( -- flag ).
     *
     * If a character is available, return true. Otherwise, return false. If non-character keyboard events are available
     * before the first valid character, they are discarded and are subsequently unavailable. The character shall be
     * returned by the next execution of KEY.
     */
    protected final static Command<JemEngine> KEY_Q = c -> c.stack
            .push(c.isCharAvailable.get() ? JemEngine.TRUE : JemEngine.FALSE);
    protected final static String[] KEY_Q_C = { "( -- flag )", "true iff a char is available." };

    /**
     * 6.1.0695 ACCEPT ( c-addr n1 -- n2 ).
     *
     * Receive a string of at most n1 characters. An ambiguous condition exists if +n1 is zero or greater than 32,767.
     * Display graphic characters as they are received. The editing functions, if any, that the system performs in order
     * to construct the string are implementation-defined.
     *
     * Input terminates when an implementation-defined line terminator is received.
     *
     * +n2 is the length of the string stored at c-addr.
     */
    protected final static Command<JemEngine> ACCEPT = c -> {};
    protected final static String[] ACCEPT_C = { "( c-addr n1 -- n2 )", "receive at most n1 characters" };

    /**
     * 6.1.1320 EMIT ( c -- )
     *
     * If x is a graphic character in the implementation-defined character set, display x. The effect of EMIT for all
     * other values of x is implementation-defined.
     */
    protected final static Command<JemEngine> EMIT = c -> c.printChar.accept(c.stack.cPop());
    protected final static String[] EMIT_C = { "( char -- )", "print the stack value interpreted as character." };

    /**
     * 6.1.0180 . "DOT" ( n -- ).
     *
     * Display n in free field format.
     */
    protected final static Command<JemEngine> DOT = c -> {};
    protected final static String[] DOT_C = { "( n -- )", "Display n in free field format." };

    /**
     * 6.1.0190 dot-quote ( "ccc&lt;quote&gt;" -- )
     *
     * create a string and compile string output to dictionary.
     */
    protected final static Command<JemEngine> DOT_QUOTE = Command.NOP;
    protected final static String[] DOT_QUOTE_C = { "( -- )",
            "create a string and compile string output to dictionary." };

    /**
     * 15.6.1.0220 .S ( -- ).
     *
     * Copy and display the values currently on the data stack.
     */
    protected final static Command<JemEngine> DOT_S = //
            c -> c.print(((c.stack.stream().map(c::formatNumber).collect(Collectors.joining(" ")))));
    protected final static String[] DOT_S_C = { "( -- )", "display the content of the data stack" };

    /**
     * 6.1.0895 CHAR ( "&lt;spaces&gt;name" -- char ).
     *
     * Skip leading space delimiters. Parse name delimited by a space. Put the value of its first character onto the
     * stack.
     */
    protected final static Command<JemEngine> CHAR = //
            c -> ofNullable(c.parseName()).filter(not(Util::isEmpty)).ifPresent(n -> c.stack.push((int) n.charAt(0)));
    protected final static String[] CHAR_C = { "( <name> -- char )", "push the first character of the pardes word" };

    /**
     * 6.1.1260 DROP ( x -- ).
     *
     * Remove x from the stack.
     */
    protected final static Command<JemEngine> DROP = c -> c.stack.pop();
    protected final static String DROP_C = "( n -- )";

    /**
     * 6.1.1290 DUP ( x -- x x ).
     *
     * Duplicate x.
     */
    protected final static Command<JemEngine> DUP = c -> c.stack.push(c.stack.peek());
    protected final static String DUP_C = "( n -- n n )";

    /**
     * 6.1.2260 SWAP ( x1 x2 -- x2 x1 ).
     *
     * Exchange the top two stack items.
     */
    protected final static Command<JemEngine> SWAP = c -> c.stack.swap();
    protected final static String SWAP_C = "( n1 n2 -- n2 n1 )";

    /**
     * 6.1.1990 OVER ( x1 x2 -- x1 x2 x1 ).
     *
     * Place a copy of x1 on top of the stack.
     */
    protected final static Command<JemEngine> OVER = c -> c.stack.push(c.stack.peek(1));
    protected final static String OVER_C = "( n1 n2 -- n1 n2 n1 )";

    /**
     * 6.1.0400 2OVER ( x1 x2 x3 x4 -- x1 x2 x3 x4 x1 x2 ).
     *
     * Copy cell pair x1 x2 to the top of the stack.
     */
    protected final static Command<ForthEngine> TWO_OVER = ForthEngine::_twoOver;
    protected final static String[] TWO_OVER_C = { "( n1 n2 n3 n4 -- n1 n2 n3 n4 n1 n2 )", "like over with two cells" };

    /**
     * 6.1.2160 ROT ( x1 x2 x3 -- x2 x3 x1 ).
     *
     * Rotate the top three stack entries.
     */
    protected final static Command<JemEngine> ROT = c -> c.stack.roll(2);
    protected final static String ROT_C = "( n1 n2 n3 -- n2 n3 n1 )";

    /**
     * 6.1.1200 DEPTH ( -- +n ).
     *
     * +n is the number of single-cell values contained in the data stack before +n was placed on the stack.
     */
    protected final static Command<JemEngine> DEPTH = c -> c.stack.push(c.stack.depth());
    protected final static String[] DEPTH_C = { "( -- n )", "Put the number of stack elements on the stack" };

    /**
     * 6.2.2030 PICK ( xu...x1 x0 u -- xu...x1 x0 xu ).
     *
     * Remove u. Copy the xu to the top of the stack. An ambiguous condition exists if there are less than u+2 items on
     * the stack before PICK is executed.
     */
    protected final static Command<JemEngine> PICK = c -> c.stack.push(c.stack.peek(c.stack.iPop()));
    protected final static String[] PICK_C = { "( n1 -- n2 )",
            "Copy the n1th stack value (not counting n1) on the stack" };

    /**
     * 6.2.2150 ROLL ( xu xu-1 ... x0 u -- xu-1 ... x0 xu ).
     *
     * Remove u. Rotate u+1 items on the top of the stack. An ambiguous condition exists if there are less than u+2
     * items on the stack before ROLL is executed.
     */
    protected final static Command<JemEngine> ROLL = c -> c.stack.roll(c.stack.iPop());
    protected final static String[] ROLL_C = { "( n1 -- n2 )",
            "Move the n1th stack value (not counting n1) to the top of the stack" };

    /**
     * 6.1.0100 ! "store" ( x a-addr -- ).
     *
     * Store x at a-addr.
     */
    protected final static Command<JemEngine> STORE = c -> c.dictionary.store(c.stack.pop(), c.stack.pop());
    protected final static String[] STORE_C = { "( n addr -- )", "store value n at address addr" };

    /**
     * 6.1.0650 @ "fetch" ( a-addr -- x ).
     *
     * x is the value stored at a-addr.
     */
    protected final static Command<JemEngine> FETCH = c -> c.stack.push(c.dictionary.fetch(c.stack.pop()));
    protected final static String[] FETCH_C = { "( addr -- n )", "Fetch the cell value stored at address addr" };

    /**
     * 6.1.0850 C! "C-store"( char c-addr -- ).
     *
     * Store char at c-addr. When character size is smaller than cell size, only the number of low-order bits
     * corresponding to character size are transferred.
     */
    protected final static Command<ForthEngine> C_STORE = c -> c.cStore(c.stack.pop(), c.stack.pop());
    protected final static String[] C_STORE_C = { "( n addr -- )", "store the lowest byte of value n at address addr" };

    /**
     * 6.1.0870 C@ "C-fetch" ( c-addr -- char ).
     *
     * Fetch the character stored at c-addr. When the cell size is greater than character size, the unused high-order
     * bits are all zeroes.
     */
    protected final static Command<ForthEngine> C_FETCH = c -> c.stack.push(c.cFetch(c.stack.pop()));
    protected final static String[] C_FETCH_C = { "( addr -- n )", "Fetch the byte stored at address addr" };

    /**
     * 17.6.1.0910 CMOVE ( c-addr1 c-addr2 u -- ).
     *
     * If u is greater than zero, copy u consecutive characters from the data space starting at c-addr1 to that starting
     * at c-addr2, proceeding character-by-character from lower addresses to higher addresses.
     */
    protected final static Command<ForthEngine> C_MOVE = ForthEngine::_cmove;
    protected final static String[] C_MOVE_C = { "( c-addr1 c-addr2 u -- )", "Move u bytes from c-addr1 to c-addr2" };

    /**
     * 17.6.1.0920 CMOVE&gt; ( c-addr1 c-addr2 u -- ).
     *
     * If u is greater than zero, copy u consecutive characters from the data space starting at c-addr1 to that starting
     * at c-addr2, proceeding character-by-character from higher addresses to lower addresses.
     */
    protected final static Command<ForthEngine> C_MOVE_UP = ForthEngine::_cmoveUp;
    protected final static String[] C_MOVE_UP_C = { "( c-addr1 c-addr2 u -- )",
            "Move u bytes from c-addr1 to c-addr2" };

    /**
     * 6.1.1540 FILL ( c-addr u char -- ).
     *
     * If u is greater than zero, store char in each of u consecutive characters of memory beginning at c-addr.
     */
    protected final static Command<ForthEngine> FILL = ForthEngine::_fill;
    protected final static String[] FILL_C = { "( c-addr u char -- )", "fill an address block with character" };

    /**
     * 6.1.0090 * "times" ( n1 | u1 n2 | u2 -- n3 | u3 ).
     *
     * Multiply n1 | u1 by n2 | u2 giving the product n3 | u3.
     */
    protected final static Command<ForthEngine> TIMES = c -> c.stack.push(c.stack.iPop() * c.stack.iPop());
    protected final static String[] TIMES_C = { "( n1 n2 -- n3 )", "n3 = n1 * n2" };

    /**
     * 6.1.0120 + "plus" ( n1 | u1 n2 | u2 -- n3 | u3 ).
     *
     * Add n2 | u2 to n1 | u1, giving the sum n3 | u3.
     */
    protected final static Command<ForthEngine> PLUS = c -> c.stack.push(c.stack.iPop() + c.stack.iPop());
    protected final static String[] PLUS_C = { "( n1 n2 -- n3 )", "n3 = n1 + n2" };

    /**
     * 6.1.0160 - ( n1 n2 -- n3 ).
     *
     * Subtract n2 from n1, giving the difference n3.
     */
    protected final static Command<ForthEngine> MINUS = ForthEngine::_minus;
    protected final static String[] MINUS_C = { "( n1 n2 -- n3 )", "n3 = n2 - n1" };

    /**
     * 6.1.1910 NEGATE ( n1 -- n2 ).
     *
     * Negate n1, giving its arithmetic inverse n2.<br>
     * : NEGATE 0 SWAP - ;
     */
    protected final static Command<ForthEngine> NEGATE = c -> c.stack.push(-c.stack.iPop());
    protected final static String[] NEGATE_C = { "( n -- n2 )", "the arithmetic inverse -- aka two's complement" };

    /**
     * 6.1.0230 / ( n1 n2 -- n3 ).
     *
     * Divide n1 by n2, giving the single-cell quotient n3. An ambiguous condition exists if n2 is zero. If n1 and n2
     * differ in sign, the implementation-defined result returned will be the same as that returned by either the phrase
     * <code>&gt;R&nbsp;S&gt;D&nbsp;R&gt;&nbsp;FM/MOD&nbsp;SWAP&nbsp;DROP</code> or the phrase
     * <code>&gt;R&nbsp;S&gt;D&nbsp;R&gt;&nbsp;SM/REM&nbsp;SWAP&nbsp;DROP</code>.
     */
    protected final static Command<ForthEngine> DIV = ForthEngine::_divide;
    protected final static String[] DIV_C = { "( n1 n2 -- n3 )", "n3 = n2 / n1" };

    /**
     * 6.1.1890 MOD ( n1 n2 -- n3 ).
     *
     * Divide n1 by n2, giving the single-cell remainder n3. An ambiguous condition exists if n2 is zero. If n1 and n2
     * differ in sign, the implementation-defined result returned will be the same as that returned by either the phrase
     * <code>R&nbsp;S&gt;D&nbsp;R&gt;&nbsp;FM/MOD&nbsp;DROP</code> or the phrase
     * <code>&gt;R&nbsp;S&gt;D&nbsp;R&gt;&nbsp;SM/REM&nbsp;DROP</code>.
     */
    protected final static Command<ForthEngine> MOD = ForthEngine::_mod;
    protected final static String[] MOD_C = { "( n1 n2 -- n3 )", "n3 = remainder of n2 / n1" };

    /**
     * 6.1.1805 LSHIFT LSHIFT ( x1 u -- x2 )
     *
     * Perform a logical left shift of u bit-places on x1, giving x2. Put zeroes into the least significant bits vacated
     * by the shift. An ambiguous condition exists if u is greater than or equal to the number of bits in a cell.
     */
    protected final static Command<ForthEngine> LSHIFT = ForthEngine::_lshift;
    protected final static String[] LSHIFT_C = { "( n1 n2 -- n3 )", "shift n1 n2 bites left" };

    /**
     * 6.1.2162 LSHIFT LSHIFT ( x1 u -- x2 )
     *
     * Perform a logical right shift of u bit-places on x1, giving x2. Put zeroes into the most significant bits vacated
     * by the shift. An ambiguous condition exists if u is greater than or equal to the number of bits in a cell.
     */
    protected final static Command<ForthEngine> RSHIFT = ForthEngine::_rshift;
    protected final static String[] RSHIFT_C = { "( n1 n2 -- n3 )", "shift n1 n2 bites right" };

    /**
     * 6.1.0320 2* ( x1 -- x2 )
     *
     * x2 is the result of shifting x1 one bit toward the most-significant bit, filling the vacated least-significant
     * bit with zero.
     */
    protected final static Command<ForthEngine> TWO_TIMES = c -> c.stack.push(c.stack.pop() << 1);
    protected final static String[] TWO_TIMES_C = { "( n1 -- n2 )", "shift n1 left one bit" };

    /**
     * 6.1.0330 2/ ( x1 -- x2 )
     *
     * x2 is the result of shifting x1 one bit toward the least-significant bit, leaving the most-significant bit
     * unchanged.
     */
    protected final static Command<ForthEngine> TWO_DIV = c -> c.stack.push(c.stack.pop() >> 1);
    protected final static String[] TWO_DIV_C = { "( n1 -- n2 )", "shift n1 right one bit" };

    /**
     * 6.1.0690 ABS ( n -- u )
     *
     * u is the absolute value of n.
     */
    protected final static Command<ForthEngine> ABS = c -> c.stack.push(Math.abs(c.stack.iPop()));
    protected final static String[] ABS_C = { "( n1 -- n2 )", "n2 is the absolute value of n1" };

    /**
     * 6.1.1870 MAX ( n1 n2 -- n3 )
     *
     * n3 is the greater of n1 and n2.
     */
    protected final static Command<ForthEngine> MAX = c -> c.stack.push(Math.max(c.stack.iPop(), c.stack.iPop()));
    protected final static String[] MAX_C = { "( n1 n2 -- n3 )", "the maximum of n1 and n2" };

    /**
     * 6.1.1880 MIN ( n1 n2 -- n3 )
     *
     * n3 is the lesser of n1 and n2.
     */
    protected final static Command<ForthEngine> MIN = c -> c.stack.push(Math.min(c.stack.iPop(), c.stack.iPop()));
    protected final static String[] MIN_C = { "( n1 n2 -- n3 )", "the minimum of n1 and n2" };

    // unsgned math

    /**
     * 2360 UM* "U-M-times" ( u1 u2 -- ud )
     *
     * Multiply u1 by u2, giving the unsigned double-cell product ud. All values and arithmetic are unsigned.<br>
     * FIXME: result is signed -- java "long" is signed
     */
    protected final static Command<ForthEngine> UM_TIMES = c -> c.stack.uPush(c.stack.uPop() * c.stack.uPop());
    protected final static String[] UM_TIMES_C = { "( u1 u2 -- u3 )", "multiply unsigned" };

    // Double math

    /**
     * 6.1.1810 M* "M-times" ( n1 n2 -- d )
     *
     * d is the signed product of n1 times n2.
     */
    protected final static Command<ForthEngine> M_TIMES = //
            c -> c.stack.dPush((long) c.stack.iPop() * (long) c.stack.iPop());
    protected final static String[] M_TIMES_C = { "( n1 n2 -- d )", "multiply to make signed double" };

    /**
     * 8.6.1.1040 D+ ( d1 | ud1 d2 | ud2 -- d3 | ud3 )
     *
     * Add d2 | ud2 to d1 | ud1, giving the sum d3 | ud3.
     */
    protected final static Command<ForthEngine> D_PLUS = c -> c.stack.dPush(c.stack.dPop() + c.stack.dPop());
    protected final static String[] D_PLUS_C = { "( d1 d2 -- d3 )", "the sum of d1 and d2" };

    /**
     * 8.6.1.1230 DNEGATE ( d1 -- d2 )
     *
     * d2 is the negation of d1.
     */
    protected final static Command<ForthEngine> D_NEGATE = c -> c.stack.dPush(-c.stack.dPop());
    protected final static String[] D_NEGATE_C = { "( d1 -- d2 )", "the negation of d1 " };

    /**
     * 8.6.1.1050 D- "D-Minus" ( d1 | ud1 d2 | ud2 -- d3 | ud3 )
     *
     * Subtract d2 | ud2 from d1 | ud1, giving the difference d3 | ud3.
     */
    protected final static Command<ForthEngine> D_MINUS = c -> c.stack.dPush(-c.stack.dPop() + c.stack.dPop());
    protected final static String[] D_MINUS_C = { "( d1 d2 -- d3 )", "d1 minus d2 -- the difference" };

    /**
     * 8.6.1.1080 D0= "D-zero-equal" ( xd -- flag )
     *
     * flag is true if and only if xd is equal to zero.
     */
    protected final static Command<ForthEngine> D_CMP_0_EQ = c -> c.stack.push(c.stack.dPop() == 0);
    protected final static String[] D_CMP_0_EQ_C = { "( d -- flag )", "true if d equals 0" };

    /**
     * 8.6.1.1075 D0&lt; "D-zero-less" ( d -- flag )
     *
     * flag is true if and only if d is less than zero.
     */
    protected final static Command<ForthEngine> D_CMP_0_LT = c -> c.stack.push(c.stack.dPop() < 0);
    protected final static String[] D_CMP_0_LT_C = { "( d -- flag )", "true if d is less than 0" };

    /**
     * 8.6.1.1120 D= "D-equal" ( xd1 xd2 -- flag )
     *
     * flag is true if and only if xd1 is bit-for-bit the same as xd2.
     */
    protected final static Command<ForthEngine> D_CMP_EQ = c -> c.stack.push(c.stack.dPop() == c.stack.dPop());
    protected final static String[] D_CMP_EQ_C = { "( d1 d2 -- flag )", "true if d equals d2" };

    /**
     * 8.6.1.1110 D&lt; "D-less" ( xd1 xd2 -- flag ).
     *
     * flag is true if and only if d1 is less than d2.
     */
    protected final static Command<ForthEngine> D_CMP_LT = c -> c.stack.push(c.stack.dPop() > c.stack.dPop());
    protected final static String[] D_CMP_LT_C = { "( d1 d2 -- flag )", "true if d1 is less than d2" };

    /**
     * 8.6.2.1270 DU&lt; ( ud1 ud2 -- flag )
     *
     * flag is true if and only if ud1 is less than ud2.<br>
     * FIXME: untested
     */
    protected final static Command<ForthEngine> D_CMP_U_LT = c -> c.stack.push(c.stack.uPop() > c.stack.uPop());
    protected final static String[] D_CMP_U_LT_C = { "( ud1 ud2 -- flag )", "true if ud1 is less than ud2" };

    /**
     * 8.6.1.1090 D2* "D-two-times" ( xd1 -- xd2 )
     *
     * xd2 is the result of shifting xd1 one bit toward the most-significant bit, filling the vacated least-significant
     * bit with zero.
     */
    protected final static Command<ForthEngine> D_TWO_TIMES = c -> c.stack.push(c.stack.dPop() << 1);
    protected final static String[] D_TWO_TIMES_C = { "( d -- flag )", "shift d one bit left" };

    /**
     * 8.6.1.1100 D2/ "D-two-div" ( xd1 -- xd2 )
     *
     * xd2 is the result of shifting xd1 one bit toward the least-significant bit, leaving the most-significant bit
     * unchanged.
     */
    protected final static Command<ForthEngine> D_TWO_DIV = c -> c.stack.push(c.stack.dPop() >> 1);
    protected final static String[] D_TWO_DIV_C = { "( d -- flag )", "shift d one bit right" };

    /**
     * 8.6.1.1160 DABS ( d -- ud )
     *
     * ud is the absolute value of d.
     */
    protected final static Command<ForthEngine> D_ABS = c -> c.stack.dPush(Math.abs(c.stack.dPop()));
    protected final static String[] D_ABS_C = { "( d1 -- d2 )", "the absolute value of d" };

    /**
     * 8.6.1.1210 DMAX ( d1 d2 -- d3 )
     *
     * d3 is the greater of d1 and d2.
     */
    protected final static Command<ForthEngine> D_MAX = c -> c.stack.dPush(Math.max(c.stack.dPop(), c.stack.dPop()));
    protected final static String[] D_MAX_C = { "( d1 d2 -- d3 )", "the maximum of d1 and d2" };

    /**
     * 8.6.1.1220 DMIN ( d1 d2 -- d3 )
     *
     * d3 is the lesser of d1 and d2.
     */
    protected final static Command<ForthEngine> D_MIN = c -> c.stack.dPush(Math.min(c.stack.dPop(), c.stack.dPop()));
    protected final static String[] D_MIN_C = { "( d1 d2 -- d3 )", "the minimum of d1 and d2" };

    /**
     * 8.6.1.1140 D&gt;S ( d -- n )
     *
     * n is the equivalent of d. An ambiguous condition exists if d lies outside the range of a signed single-cell
     * number.
     */
    protected final static Command<ForthEngine> D_TO_S = c -> c.stack.iPush((int) c.stack.dPop());
    protected final static String[] D_TO_S_C = { "( d1 d2 -- d3 )", "( d -- n )", "convert double to int" };

    /**
     * 6.1.0630 ?DUP (x -- 0 | x x ).
     *
     * Duplicate x if it is non-zero.
     */
    protected final static Command<ForthEngine> Q_DUP = ForthEngine::_questionDupe;
    protected final static String[] Q_DUP_C = { "( n -- 0 | n n )", "duplicate if not zero." };

    /**
     * 6.1.0240 /MOD ( n1 n2 -- n3 n4 ).
     *
     * Divide n1 by n2, giving the single-cell remainder n3 and the single-cell quotient n4. An ambiguous condition
     * exists if n2 is zero.
     */
    protected final static Command<ForthEngine> SLASH_MOD = ForthEngine::_slashMod;
    protected final static String[] SLASH_MOD_C = { "( n1 n2 -- n3 n4 )",
            "devide n1 by n2, n3 is the remainder and n4 the quotient" };

    /**
     * 6.1.2370 UM/MOD ( ud u1 -- u2 u3 ).
     * <p>
     * Divide ud by u1, giving the quotient u3 and the remainder u2. All values and arithmetic are unsigned. An
     * ambiguous condition exists if u1 is zero or if the quotient lies outside the range of a single-cell unsigned
     * integer.<br>
     *
     * FIXME: ud should be unsigned long
     */
    protected final static Command<ForthEngine> UM_SLASH_MOD = ForthEngine::_umSlashMod;
    protected final static String[] UM_SLASH_MOD_C = { "( u1 u2 -- u3 44 )",
            "devide u1 by u2, u3 is the remainder and u4 the quotient" };

    /**
     * 6.1.0100 &#42;&#47; ( n1 n2 n3 -- n4 ).
     *
     * Multiply n1 by n2 producing the intermediate double-cell result d. Divide d by n3 giving the single-cell quotient
     * n4. An ambiguous condition exists if n3 is zero or if the quotient n4 lies outside the range of a signed number.
     */
    protected final static Command<ForthEngine> TIMES_DIVIDE = ForthEngine::_timesDivide;
    protected final static String[] TIMES_DIVIDE_C = { "( n1 n2 n3 -- n4 )", " n4 = (n1 * n2) / n3" };

    /**
     * 6.1.0110 &#42;&#47;MOD ( n1 n2 n3 -- n4 n5 ).
     *
     * Multiply n1 by n2 producing the intermediate double-cell result d. Divide d by n3 producing the single-cell
     * remainder n4 and the single-cell quotient n5. An ambiguous condition exists if n3 is zero, or if the quotient n5
     * lies outside the range of a single-cell signed integer.
     */
    protected final static Command<ForthEngine> TIMES_DIVIDE_MOD = ForthEngine::_timesDivMod;
    protected final static String[] TIMES_DIVIDE_MOD_C = { "( n1 n2 n3 -- n4 n5 )",
            "calculate (n1 * n2) / n3, n4 is the remainder and n5 the quotient" };

    /**
     * non-std STRING ( &lt;name&gt; -- ).
     *
     * Creates a dictionary entry for a new string word.
     */
    protected static final Command<JemEngine> CREATE_STRING = c -> c.dictionary.create(new StringWord(c.parseName()));
    protected final static String[] CREATE_STRING_C = { "( <name> -- )", "create a counted string word" };

    /**
     * non-std TIME( -- d ).
     *
     * push the current system time in miliseconds as double int value.
     */
    protected static final Command<JemEngine> TIME = c -> c.stack.dPush(System.currentTimeMillis());
    protected final static String[] TIME_C = { "( -- d )", "current time in milli seconds" };

    /**
     * 6.1.2170 S&gt;D "S-to-D" ( n -- d )
     *
     * Convert the number n to the double-cell number d with the same numerical value.
     */
    protected static final Command<JemEngine> S_TO_D = c -> c.stack.dPush(c.stack.iPop());
    protected final static String[] S_TO_D_C = { "( n -- d )", "extend integer to double" };

    {
        add(new StringWord("PAD").comment("scratch pad")); // 6.2.2000
    }

    /**
     * 6.1.2340 U&lt; "u-less" ( u1 u2 -- flag ).
     *
     * flag is true if and only if u1 is less than u2.
     */
    protected static final Command<JemEngine> U_CMP_LT = c -> c.stack.push(c.stack.uPop() > c.stack.uPop());
    protected final static String[] U_CMP_LT_C = { "( u1 u2 -- flag )", "true if u1 is less than u2" };

    /**
     * 6.2.2350 U&gt; "u-more" ( u1 u2 -- flag )
     *
     * flag is true if and only if u1 is greater than u2.
     */
    protected static final Command<JemEngine> U_CMP_GT = c -> c.stack.push(c.stack.uPop() < c.stack.uPop());
    protected final static String[] U_CMP_GT_C = { "( u1 u2 -- flag )", "true if u1 is greater than u2" };

    /**
     * 15.6.1.1280 DUMP ( addr u -- ).
     *
     * Display the contents of u consecutive addresses starting at addr.<br>
     * The format of the display is implementation dependent.
     */
    protected static final Command<ForthEngine> DUMP = c -> c._dump();
    protected final static String[] DUMP_C = { "( addr u -- )", "dump n bytes strating at addr" };

    /**
     * 15.6.1.2194 SEE ( "&lt;spaces&gt;name" -- ).
     *
     * Display a human-readable representation of the named word's definition.
     */
    protected static final Command<ForthEngine> SEE = c -> c.print(c.inspector.see(c.find(c.parseName())));
    protected final static String[] SEE_C = { "( <name> -- )", "display the word's definition" };

    /**
     * non-Std SEEE ( "&lt;spaces&gt;name" -- ).
     *
     * Try to decompile a word's definition
     */
    protected static final Command<ForthEngine> SEEE = c -> c.print(c.inspector.deCompile(c.find(c.parseName())));
    protected final static String[] SEEE_C = { "( <name> -- )", "try to decompile the word" };

    /**
     * 15.6.1.2465 WORDS ( -- ).
     *
     * List the definition names in the first word list of the search order.<br>
     * The format of the display is implementation-dependent.
     */
    protected static final Command<ForthEngine> WORDS = c -> c.print(c.inspector.words());
    protected final static String[] WORDS_C = { "( <name> -- )", "list word definitions" };

    /**
     * 15.6.1.0600 ? "Q" ( a-addr -- ).
     *
     * Display the value stored at a-addr.
     */
    protected static final Command<ForthEngine> Q = c -> {}; // c.fetch(c.inspector.words());
    protected final static String[] Q_C = { "( addr -- )", "display the value stored at addr" };

    /**
     * 17.6.1.0170 -TRAILING ( c-addr u1 -- c-addr u2 )
     *
     * If u1 is greater than zero, u2 is equal to u1 less the number of spaces at the end of the character string
     * specified by c-addr u1. If u1 is zero or the entire string consists of spaces, u2 is zero.
     */
    protected static final Command<ForthEngine> DASH_TRAILING = ForthEngine::_dashTrailing;
    protected final static String[] DASH_TRAILING_C = { "( c-addr u1 -- c-addr u2 )",
            "calculate string length without trailing spaces" };

    /**
     * Defer reading of a byte to the memory sub system.
     *
     * @param address
     *            address of te byte to read
     * @return the byte read
     */
    public int cFetch(int address) {
        return dictionary.cFetch(address);
    }

    /**
     * Defer writing of a byte to memory the sub system.
     *
     * @param address
     *            address of the byte to store
     * @param value
     *            value to store (in the lower byte)
     */
    public void cStore(int address, int value) {
        dictionary.cStore(address, value);
    }

    /**
     * Duplicate tos if it is non-zero.
     */
    protected void _questionDupe() {
        if (stack.iPeek(0) != 0) {
            stack.push(stack.peek(0));
        }
    }

    protected void _bracketTickBracket() {
        Word word = find(parseName());
        comma(litWord.xt);
        comma(word.xt);
    }

    /**
     * like over with two cells.
     */
    protected void _twoOver() {
        stack.push(stack.peek(3));
        stack.push(stack.peek(3));
    }

    protected void _dSlashMod() {
        int div = stack.iPop();
        long n = stack.dPop();
        stack.iPush((int) (n % div));
        stack.dPush(n / div);
    }

    /**
     * From the xt compute the (first) pfa
     */
    protected void _toBody() {
        Word word = dictionary.findWordContainingPfa(stack.pop());
        if (word != null) {
            stack.push(word.xt + CELL_SIZE);
        } else {
            throw new IllegalMemoryAccessException();
        }
    }

    /**
     * devide and push remainder and quotient.
     */
    protected void _slashMod() {
        int n2 = stack.iPop();
        int n1 = stack.iPop();
        stack.push(n1 % n2);
        stack.push(n1 / n2);
    }

    /**
     * multiply and divide.
     */
    protected void _timesDivide() {
        int n3 = stack.iPop();
        long n1n2 = stack.uPop() * stack.uPop();
        stack.push((int) (n1n2 / n3));
    }

    /**
     * like slashmod for unsigned int.
     */
    protected void _umSlashMod() {
        long u1 = stack.uPop();
        long ud = stack.dPop();
        stack.uPush(ud % u1);
        stack.uPush(ud / u1);
    }

    /**
     * like _timesdivide with remainder and quotient;
     */
    protected void _timesDivMod() {
        int n3 = stack.pop();
        long n1n2 = (long) stack.pop() * stack.pop();
        stack.push((int) (n1n2 / n3));
        stack.push((int) (n1n2 % n3));
    }

    /**
     * subtract the two top values.
     */
    protected void _minus() {
        int n2 = stack.iPop();
        int n1 = stack.iPop();
        stack.push(n1 - n2);
    }

    /**
     * devide the top two values.
     */
    protected void _divide() {
        int n2 = stack.iPop();
        int n1 = stack.iPop();
        stack.push(n1 / n2);
    }

    /**
     * devide the top two values and push the remainder.
     */
    protected void _mod() {
        int n2 = stack.iPop();
        int n1 = stack.iPop();
        stack.push(n1 % n2);
    }

    protected void _lshift() {
        int u = stack.iPop();
        long x1 = stack.uPop();
        stack.push(x1 << u);
    }

    protected void _rshift() {
        int u = stack.iPop();
        long x1 = stack.uPop();
        stack.push(x1 >> u);
    }

    protected void _cmove() {
        int count = stack.pop();
        int addr2 = stack.pop();
        int addr1 = stack.pop();
        for (int n = 0; n < count; n++) {
            cStore(addr2 + n, cFetch(addr1 + n));
        }
    }

    protected void _cmoveUp() {
        int count = stack.pop();
        int addr2 = stack.pop();
        int addr1 = stack.pop();
        for (int n = count - 1; n >= 0; n--) {
            cStore(addr2 + n, cFetch(addr1 + n));
        }
    }

    protected void _fill() {
        int zch = stack.pop();
        int count = stack.pop();
        int start = stack.pop();
        for (int n = 0; n < count; n++) {
            int current = start + n;
            cStore(current, zch);
        }
    }

    protected void _dashTrailing() {
        int len = stack.iPop();
        int adr = stack.iPop();
        while (len > 0) {
            int c = cFetch(adr + len - 1);
            if (c > 0 && !Character.isWhitespace(c)) {
                break;
            }
            len--;
        }
        stack.push(adr);
        stack.push(len);
    }

    protected void _dump() {

    }

    /**
     * terminate execution.
     */
    protected void _bye() {
        throw new ForthTerminatedException();
    }

}
