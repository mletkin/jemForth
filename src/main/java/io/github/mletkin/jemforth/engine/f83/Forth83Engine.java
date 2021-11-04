package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.MemoryMapper.CELL_SIZE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;

import io.github.mletkin.jemforth.engine.Command;
import io.github.mletkin.jemforth.engine.ForthEngine;
import io.github.mletkin.jemforth.engine.JemEngine;
import io.github.mletkin.jemforth.engine.Util;
import io.github.mletkin.jemforth.engine.exception.ForthTerminatedException;
import io.github.mletkin.jemforth.engine.exception.JemForthException;
import io.github.mletkin.jemforth.engine.words.StringWord;
import io.github.mletkin.jemforth.engine.words.UserVariableWord;
import io.github.mletkin.jemforth.engine.words.VocabularyWord;
import io.github.mletkin.jemforth.engine.words.Word;

/**
 * Engine for Standard Forth 83.
 * <p>
 * This engine empulates block buffer access to the file system. The Methods
 * cFetch and cStore are overridden to access the block system.
 *
 * @see <a href="http://forth.sourceforge.net/standard/fst83/">Forth 83
 *      Standard</a>
 **/
public class Forth83Engine extends ForthEngine {

    protected BlockBuffer blockBuffer = new BlockBuffer("c:\\data\\massStorage");
    protected StringWord hld = new StringWord("HLD");

    protected int blk = 0;
    protected int scr = 0;
    protected int span = 0;

    /**
     * Keeps the word implementing the forth KEY function.
     */
    protected Word keyWord;

    /**
     * The current file to be read, if any.
     */
    private FileInputStream fis;

    // will be changed to the "real" executor later during construction
    private Consumer<Forth83Engine> executor = this::executeInternal;

    public Forth83Engine() {

        // Missing 2012 Std core words
        // 6.1.0570 >NUMBER
        // 6.1.0705 ALIGN
        // 6.1.0706 ALIGNED
        // 6.1.1345 ENVIRONMENT?
        // 6.1.1360 EVALUATE
        // 6.1.1380 EXIT
        // 6.1.1900 MOVE
        // 6.1.2216 SOURCE
        // 6.1.2380 UNLOOP
        // 6.2.0455 :NONAME
        // 6.2.0620 ?DO
        // 6.2.0698 ACTION-OF
        // 6.2.0873 CASE
        // 6.2.0945 COMPILE,
        // 6.2.1173 DEFER
        // 6.2.1175 DEFER!
        // 6.2.1177 DEFER@
        // 6.2.1342 ENDCASE
        // 6.2.1343 ENDOF
        // 6.2.1675 HOLDS
        // 6.2.1725 IS
        // 6.2.1850 MARKER
        // 6.2.1950 OF
        // 6.2.2008 PARSE
        // 6.2.2020 PARSE-NAME
        // 6.2.2125 REFILL
        // 6.2.2148 RESTORE-INPUT
        // 6.2.2266 S\"
        // 6.2.2182 SAVE-INPUT
        // 6.2.2218 SOURCE-ID
        // 6.2.2295 TO
        // 6.2.2395 UNUSED
        // 6.2.2405 VALUE
        // 6.2.2440 WITHIN

        // 17.6.1.0245 /STRING
        // 17.6.1.0935 COMPARE
        // 17.6.1.2191 SEARCH
        // 17.6.1.2212 SLITERAL

        add(hld.comment("buffer for pictured number output"));
        add(new UserVariableWord("BLK", () -> this.blk, v -> this.blk = v)); // 7.6.1.0790
        add(new UserVariableWord("SCR", () -> this.scr, v -> this.scr = v)); // 7.6.2.2190

        // Stack manipulation
        add("DUP", ForthEngine.DUP);
        add("DROP", ForthEngine.DROP);
        add("SWAP", ForthEngine.SWAP);
        add("OVER", ForthEngine.OVER);
        add("ROT", ForthEngine.ROT);

        add("?DUP", ForthEngine.Q_DUP);
        add("DEPTH", ForthEngine.DEPTH);
        add("PICK", ForthEngine.PICK);
        add("ROLL", ForthEngine.ROLL);

        // Compilation / Dictionary / Definition
        add(":", ForthEngine.COLON);
        add(";", ForthEngine.SEMICOLON).immediate();
        add("'", ForthEngine.TICK);
        add("IMMEDIATE", ForthEngine.IMMEDIATE).immediate();

        addF("COMPILE", Forth83Engine::_compile);
        add("[COMPILE]", ForthEngine.POSTPONE).immediate();

        add("CREATE", ForthEngine.CREATE);
        add("ALLOT", ForthEngine.ALLOT);
        add("VARIABLE", ForthEngine.VARIABLE);
        add("CONSTANT", ForthEngine.CONSTANT);
        add("WORD", ForthEngine.WORD);
        add("FIND", ForthEngine.FIND);
        add(">BODY", ForthEngine.TO_BODY);

        add("!", ForthEngine.STORE);
        add("@", ForthEngine.FETCH);

        add("C!", ForthEngine.C_STORE);
        add("C@", ForthEngine.C_FETCH);

        // Vocabularies are specific for Forth 83 and not part of the ANS-Standard
        // The FENCE/FORGET mechanism are legacy words in 2012
        add(dictionary.fenceWord("FENCE"));
        add("FORGET", ForthEngine.FORGET);
        add(new UserVariableWord("CONTEXT", //
                () -> dictionary.getSearchResolver().getOrder().findFirst().orElse(null), //
                v -> dictionary.getSearchResolver().setOrder(v)));
        add(new UserVariableWord("CURRENT", //
                () -> dictionary.getSearchResolver().getCurrent(), //
                v -> dictionary.getSearchResolver().setCurrent(v)));
        addF("VOCABULARY", c -> c.dictionary.add(new VocabularyWord(c.parseName()))) //
                .comment("( <name> -- )", "create a vocabulary with the given name");
        add(": DEFINITIONS CONTEXT @ CURRENT ! ;");

        add(": BUFFER: CREATE ALLOT ;").comment(ForthEngine.BUFFER_COLON.comment());

        // logic operators

        add("AND", ForthEngine.BIT_AND);
        add("OR", ForthEngine.BIT_OR);
        add("XOR", ForthEngine.BIT_XOR);
        add("INVERT", ForthEngine.BIT_INVERT);
        add(": FALSE 0 ; "); // 6.2.1485
        add(": TRUE 0 INVERT ; "); // 6.2.2298

        // NOT is not standardized.
        // F79 prefers 1's complement, while 2012 prefers 2's complement
        // The use in if..then constructs depends on the implementation of TRUE as 1 or
        // -1
        // We use biswise inversion here
        add("NOT", ForthEngine.BIT_INVERT);

        // comparison operators
        add("=", ForthEngine.CMP_EQ);
        add("<", ForthEngine.CMP_LT);
        add(">", ForthEngine.CMP_GT);

        add("0=", ForthEngine.CMP_0_EQ);
        add("0<", ForthEngine.CMP_0_LT);
        add("0>", ForthEngine.CMP_0_GT);
        add("0<>", ForthEngine.CMP_0_NE);

        add(": <> = NOT ;").comment(ForthEngine.CMP_NE.comment());
        add(": <= > NOT ;");
        add(": >= < NOT ;");

        // signed single cell math operations
        add("+", ForthEngine.PLUS);
        add("*", ForthEngine.TIMES);
        add("-", ForthEngine.MINUS);
        add("/", ForthEngine.DIV);
        add("MOD", ForthEngine.MOD);

        add(": 1+ 1 + ;"); // 6.1.0290
        add(": 1- 1 - ;"); // 6.1.0300
        add(": 2+ 2 + ;");
        add(": 2- 2 - ;");

        add(",", ForthEngine.COMMA);
        add("C,", ForthEngine.C_COMMA);

        // return stack access
        add(">R", JemEngine.R_TO);
        add("R>", JemEngine.R_FROM);
        add("R@", JemEngine.R_FETCH);
        add("RDROP", JemEngine.R_DROP);
        add("RP!", JemEngine.R_CLEAR);
        add(".RSTACK", JemEngine.DOT_RSTACK);

        addF("I", c -> c.rPeek(2)); // 6.1.1680
        addF("J", c -> c.rPeek(4)); // 6.1.1730
        addF("K", c -> c.rPeek(6)); // non std

        add(": CELLS " + CELL_SIZE + " * ;"); // 6.1.0890
        add(": CELL+ " + CELL_SIZE + " + ;"); // 6.1.0880
        // 6.1.0897 CHAR+
        // 6.1.0898 CHARS

        // double cell stack manipulation
        add(": 2DUP OVER OVER ;").comment("( n1 n2 -- n1 n2 n1 n2 )"); // 6.1.03380
        add(": 2DROP DROP DROP ;").comment("( n1 n2 -- )"); // 6.1.0370
        add(": 2SWAP ROT >R ROT R> ;").comment("( n1 n2 n3 n4 -- n3 n4 n1 n2 )"); // 6.1.0430
        add(": 2OVER >R >R 2DUP R> R> 2SWAP ;").comment(ForthEngine.TWO_OVER.comment()); // 6.1.0400
        add(": 2ROT >R >R 2SWAP R> R> 2SWAP ;");

        add(": 2>R SWAP >R >R ;") // 6.2.0340
                .comment("( x1 x2 -- ) ( R: -- x1 x2 )", "Transfer cell pair x1 x2 to the return stack.");
        add(": 2R> R> R> SWAP ;") // 6.2.0410
                .comment("( -- x1 x2 ) ( R: x1 x2 -- ) ", "Transfer cell pair x1 x2 from the return stack.");
        add(": 2R@ R> R> 2DUP >R >R SWAP ;") // 6.2.0415
                .comment("( -- x1 x2 ) ( R: x1 x2 -- x1 x2 )", "Copy x1 x2 from the return stack.");

        add(new UserVariableWord("DP", dictionary::getHereValue, READ_ONLY));
        add(": HERE DP @ ;").comment(ForthEngine.HERE.comment());

        // words for flow control
        add(": BEGIN  HERE ; IMMEDIATE"); // 6.1.0760
        add(": UNTIL  COMPILE ?BRANCH , ; IMMEDIATE"); // 6.1.2390
        add(": END    [COMPILE] UNTIL ; IMMEDIATE"); // alias for until
        add(": LEAVE  COMPILE BRANCH HERE SWAP 0 , ; IMMEDIATE"); // 6.1.1760
        add(": AGAIN  COMPILE BRANCH , ; IMMEDIATE"); // 6.2.0700

        add(": WHILE  COMPILE ?BRANCH HERE 0 , ; IMMEDIATE"); // 6.1.2430
        add(": REPEAT COMPILE BRANCH  HERE CELL+ SWAP ! , ; IMMEDIATE"); // 6.1.2140

        add(": IF     COMPILE ?BRANCH HERE 0 , ; IMMEDIATE"); // 6.1.1700
        add(": THEN   HERE SWAP ! ; IMMEDIATE"); // 6.1.2270
        add(": ELSE   COMPILE BRANCH  HERE 0 , SWAP HERE SWAP ! ; IMMEDIATE"); // 6.1.1310

        add(": DO HERE COMPILE >R COMPILE >R ; IMMEDIATE"); // 6.1.1240
        add(": LOOP COMPILE R> COMPILE R> COMPILE 1+  COMPILE 2DUP COMPILE = COMPILE ?BRANCH , COMPILE 2DROP ; IMMEDIATE"); // 6.1.1800
        add(": +LOOP COMPILE R> COMPILE R> COMPILE ROT COMPILE + COMPILE 2DUP COMPILE = COMPILE ?BRANCH , COMPILE 2DROP ; IMMEDIATE"); // 6.1.0140

        add(": >MARK HERE 0 , ;");
        add(": >RESOLVE HERE SWAP ! ;");

        add(": <MARK HERE ;");
        add(": <RESOLVE , ;");

        add("RECURSE", ForthEngine.RECURSE).immediate();

        // I/O
        add("EMIT", ForthEngine.EMIT);
        add(": COUNT 1+ DUP 1- C@ ;").comment("( a1 -- a2 u )", // 6.1.0980
                "Push length and address of first char for the string address on the stack.");

        add(": TYPE ?DUP IF OVER + SWAP DO I C@ EMIT LOOP ELSE DROP THEN ;") // 6.1.2310
                .comment("( addr len -- )", "print String with address of first char and length");

        addF(">STRING", Forth83Engine::copyToString); // non-Standard
        add(": C\" $22 WORD >STRING COMPILE (STRLITERAL) , ; IMMEDIATE"); // 6.2.0855
        add(": .\" STATE @ IF [COMPILE] C\" COMPILE COUNT COMPILE TYPE ELSE $22 WORD COUNT TYPE THEN ; IMMEDIATE");
        add(": S\" $22 WORD >STRING COUNT SWAP COMPILE (STRLITERAL) , COMPILE (LITERAL) , ; IMMEDIATE"); // 6.1.2165

        // single precision integer math
        add("*/", ForthEngine.TIMES_DIVIDE);
        add("*/MOD", ForthEngine.TIMES_DIVIDE_MOD);
        add("/MOD", ForthEngine.SLASH_MOD);
        add("2*", ForthEngine.TWO_TIMES);
        add("2/", ForthEngine.TWO_DIV);
        add("ABS", ForthEngine.ABS);
        add("MAX", ForthEngine.MAX);
        add("MIN", ForthEngine.MIN);
        add("NEGATE", ForthEngine.NEGATE);
        add("LSHIFT", ForthEngine.LSHIFT);
        add("RSHIFT", ForthEngine.RSHIFT);
        // FM/MOD 6.1.1561
        // SM/REM 6.1.2214

        // double precision integer math
        add("DNEGATE", ForthEngine.D_NEGATE);
        add("D+", ForthEngine.D_PLUS);
        add("D-", ForthEngine.D_MINUS); // : D- DNEGATE D+ ;
        add("D0=", ForthEngine.D_CMP_0_EQ); // : D0= OR 0= ;
        add("D0<", ForthEngine.D_CMP_0_LT);
        add("D=", ForthEngine.D_CMP_EQ); // : D= D- D0= ;
        add("D<", ForthEngine.D_CMP_LT);
        add("D2*", ForthEngine.D_TWO_TIMES);
        add("D2/", ForthEngine.D_TWO_DIV);
        add("DABS", ForthEngine.D_ABS); // : DABS DUP 0< IF DNEGATE THEN ;

        add("DMAX", ForthEngine.D_MAX);
        add("DMIN", ForthEngine.D_MIN);
        add("D>S", ForthEngine.D_TO_S);
        add("DU<", ForthEngine.D_CMP_U_LT);

        add("M*", ForthEngine.M_TIMES);
        // M*/ 94: 8.6.1.1820
        // M+ 94: 8.6.1.1830

        // unsigned integer math
        add("U<", ForthEngine.U_CMP_LT);
        add("U>", ForthEngine.U_CMP_GT);
        add("UM*", ForthEngine.UM_TIMES);
        add("UM/MOD", ForthEngine.UM_SLASH_MOD);

        // output/character/String
        add(": CR #10 #13 EMIT EMIT ;"); // 6.1.0990
        add(": BL 20 ;"); // 6.1.0770
        add(": SPACE BL EMIT ;"); // 6.1.2220
        add(": SPACES BEGIN DUP 0> WHILE SPACE 1- REPEAT DROP ;"); // 6.1.2230

        add(": .( 29 WORD COUNT TYPE ; IMMEDIATE"); // 6.2.0200

        add("FILL", Forth83Engine.FILL);
        add(": BLANK BL FILL ;").comment("( addr u -- "); // 17.6.1.0780
        add(": ERASE 0 FILL ;").comment("( addr u -- "); // 6.2.1350
        add("CMOVE", ForthEngine.C_MOVE);
        add("CMOVE>", ForthEngine.C_MOVE_UP);

        add(": +! DUP @ ROT + SWAP ! ;").comment("( n1 n2 -- )"); // 6.1.0130 not tested
        add(": 2! DUP >R ! R> CELL+ ! ;"); // 6.1.0310 not tested
        add(": 2@ DUP @ >R CELL+ @ R> ;"); // 6.1.0350 not tested

        add(": 2VARIABLE CREATE 2 CELLS ALLOT ;"); // 8.6.1.0440 not tested
        add(": 2CONSTANT CREATE , , DOES> 2@ ;"); // 8.6.1.0360 not tested
        // 2VALUE 8.6.2.0435

        keyWord = add("KEY", ForthEngine.KEY);
        add("KEY?", ForthEngine.KEY_Q);
        add(": ( 29 WORD DROP ; IMMEDIATE"); // 6.1.0080
        add("HEX", ForthEngine.BASE_HEX);
        add("DECIMAL", ForthEngine.BASE_DECIMAL);
        add("OCTAL", ForthEngine.BASE_OCTAL);
        add("BIN", ForthEngine.BASE_BIN);

        add("EXECUTE", ForthEngine.EXECUTE);
        add(": LITERAL STATE @ IF COMPILE (LITERAL) , THEN ; IMMEDIATE"); // 6.1.1780 from figForth
        add("[", ForthEngine.BRACKET).immediate();
        add("]", ForthEngine.RIGHT_BRACKET);

        addF(">LITERAL", Forth83Engine::toLiteral);

        add("[']", ForthEngine.BRACKET_TICK_BRACKET).immediate();

        addF("D/MOD", Forth83Engine::_dSlashMod);

        // pictured number formatting
        addF("HOLD", c -> c.hld.prepend(c.stack.pop())) // 6.1.1670
                .comment("( char -- )", "Add a character to the pictured numeric outputput string buffer.");
        add("<#", c -> hld.clear()).comment("( -- )", "Initialize pictured numeric output conversion."); // 6.1.0490
        add(": # BASE @ D/MOD ROT 9 OVER < IF #55 ELSE #48 THEN + HOLD ;"); // 6.1.0030
        add(": #S BEGIN # OVER OVER OR 0= UNTIL ;"); // 6.1.0050
        add(": SIGN  ROT 0< IF $2D HOLD THEN ;"); // 6.1.2210
        add(": #> DROP DROP HLD COUNT ;"); // 6.1.0040

        add(": D.R >R " //
                + "SWAP OVER DABS " //
                + "<# #S SIGN #> " //
                + "R> OVER - SPACES TYPE ;").comment("( d n -- )", "Print double number right aligned"); // 8.6.1.1070

        add("S>D", ForthEngine.S_TO_D); // not_83_std
        add("U->D", c -> stack.dPush(stack.uPop())).comment("( u -- d )", "extend unsigned to double"); // not_83_std
        add(": .R >R S>D R> D.R ;"); // 6.2.0210

        add(": D. SWAP OVER DABS " //
                + "<# #S SIGN #> " //
                + "TYPE SPACE ;").comment("( d -- )", "Print double followed by a space"); // 8.6.1.1060
        add(": .  S>D D. ;").comment(ForthEngine.DOT.comment());
        add(": U. U->D D. ;").comment("( u -- )", "Print unsigned integer followed by a space"); // 6.1.2620

        add(": U.R >R U->D R> D.R ;")// 6.2.2330
                .comment("( u n -- )", "Print unsigned right aligned in a field n characters wide.");

        // not in F83 standard
        add(": DLITERAL STATE @ IF SWAP [COMPILE] LITERAL " + // figForth 79
                "[COMPILE] LITERAL THEN ; IMMEDIATE");

        add(": \\ BEGIN KEY DUP 0 = SWAP 10 = OR UNTIL ; IMMEDIATE"); // 6.2.2230
        add("STRING", ForthEngine.CREATE_STRING);

        add(".S", ForthEngine.DOT_S);

        addF("SP!", c -> c.stack.clear());

        add("CHAR", ForthEngine.CHAR);
        add("[CHAR]", ForthEngine.BRACKET_CHAR).immediate();

        add("SEE", ForthEngine.SEE);
        add("SEEE", Forth83Engine.SEEE); // non-std
        add("WORDS", ForthEngine.WORDS);
        add("DUMP", Forth83Engine.DUMP);

        add(": ? @ . ;").comment(ForthEngine.Q.comment());

        add(": NIP SWAP DROP ;").comment("( n1 n2 -- n2 )"); // 6.2.1930
        add(": TUCK SWAP OVER ;").comment("( n1 n2 -- n2 n1 n2 )"); // 6.2.2300

        // words for block buffer operations
        addF("BLOCK", c -> c.stack.push(c.blockBuffer.block(c.stack.iPop()))); // 7.6.1.0800
        addF("BUFFER", c -> c.stack.push(c.blockBuffer.assignBuffer(c.stack.iPop()))); // 7.6.1.0820
        addF("SAVE-BUFFERS", c -> c.blockBuffer.saveUpdated()); // 7.6.1.2180
        addF("FLUSH", c -> c.blockBuffer.flushBuffers()); // 7.6.1.1559
        addF("EMPTY-BUFFERS", c -> c.blockBuffer.emptyBuffers()); // 7.6.2.1330
        addF("LIST", Forth83Engine::_list); // 7.6.2.1770
        addF("UPDATE", c -> c.blockBuffer.update()); // 7.6.1.2400
        // 7.6.1.1360 EVALUATE
        // 7.6.2.2125 REFILL
        // 6.2.2535/7.6.2.2535 \

        add(": RUN ?DUP IF   STATE @ <> IF EXECUTE ELSE , THEN" + //
                "       ELSE >LITERAL " + //
                "            IF   STATE @ IF COMPILE (LITERAL) , THEN " + //
                "            ELSE COUNT TYPE #63 EMIT " + //
                "            THEN " + //
                "      THEN ;").comment("execute after WORD FIND have been executed");

        Word intWord = add(": INTERPRET BEGIN BLK @ >IN @ #TIB @ < OR WHILE BL WORD FIND RUN REPEAT ;") //
                .comment("( -- )", "Interpret all words from the current input stream");

        add(": LOAD BLK ! 0 >IN ! INTERPRET ;"); // 7.6.1.1790
        add(": THRU 1+ SWAP DO I LOAD LOOP ;"); // 7.6.2.2280
        add(": --> BLK DUP @ ?DUP IF 1 + SWAP ! LOAD ELSE DROP THEN ; IMMEDIATE") //
                .comment("( -- )", "load the next block");

        addF("INCLUDE", c -> c._include(c.parseName())).comment("( <name> -- )", "process the specified file");
        add("-TRAILING", ForthEngine.DASH_TRAILING);

        // span keeps the number of character read
        add(new UserVariableWord("SPAN", () -> span, v -> span = v));

        addF("EXPECT", Forth83Engine::_expect).comment("( addr n -- )");
        // fig : QUERY TIB @ 50 EXPECT 0 IN ! ;
        add(": QUERY TIB #80 EXPECT SPAN @ TIB C! 0 >IN ! ;")
                .comment("read max. 80 characters into the TIB and reset >IN");
        add(": QUIT [COMPILE] [ BEGIN RP! CR '>' EMIT QUERY BL EMIT INTERPRET STATE @ 0= IF .\" OK\" THEN AGAIN ;"); // 6.1.2050
        add(": ABORT SP! DECIMAL [COMPILE] FORTH DEFINITIONS QUIT ;"); // 6.1.0670
        add(": ABORT\" [COMPILE] IF [COMPILE] .\" COMPILE ABORT [COMPILE] THEN ; IMMEDIATE"); // 6.1.0680

        add("BYE", ForthEngine.BYE);

        add(": FORTH-83 ;");

        add("TIME", ForthEngine.TIME); // non-std
        add("VOCABULARY EDITOR"); // 15.6.2.1300

        // initialize engine
        add("#10 BASE !");
        add("' TIME FENCE !");

        // from here on we use the FORTH interpreter
        executor = c -> execute(intWord);
    }

    /**
     * Creates a new string word from the string whose address is on the stack.
     */
    private void copyToString() {
        StringWord source = (StringWord) dictionary.findWordContainingPfa(stack.pop());
        StringWord newWord = new StringWord("");
        newWord.setData(source.data());
        stack.push(dictionary.add(newWord).xt() + 1);
    }

    @Override
    public void reset(boolean executionOnly) {
        super.reset(executionOnly);
        blk = 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Copies the input string into the terminal input buffer and executes the
     * INTERPRET word.
     */
    @Override
    public void process(String input) {
        tibWord.setData(input);
        toIn = 0;
        executor.accept(this);
    }

    /**
     * Fills the terminal input buffer and execute the java coded interpreter.
     * <p>
     * Used for boot strappping when the real INTERPRET word is not available yet.
     * This works for compiling certain colon definitions, but might cause problems,
     * when the execution involves words like INTERPRET.
     *
     * @param engine
     */
    private void executeInternal(Forth83Engine engine) {
        while (toIn < tibWord.length()) {
            stack.push(32);
            _word(); // c-addr
            _find(); // c-addr 0 | xt 1 | xt -1
            _run();
        }
    }

    /**
     * Exception free processes a single word from the terminal input buffer.
     */
    protected void _run() {
        try {
            doRun();
        } catch (ForthTerminatedException e) {
            throw e;
        } catch (JemForthException e) {
            print(e.getMessage());
        }
    }

    /**
     * Processes the result of WORD and FIND.
     *
     * TODO: handle double literals properly
     */
    protected void doRun() {
        DUP.cmd().execute(this);
        if (!FIND_NOT.equals(stack.iPop())) { // if found
            if (state == COMPILE) { // if compile
                if (FIND_IMMEDIATE.equals(stack.iPop())) { // if immediate
                    execute(dictionary.findWordContainingPfa(stack.pop()));
                } else {
                    COMPILE_COMMA.cmd().execute(this);
                }
            } else {
                stack.pop();
                execute(dictionary.findWordContainingPfa(stack.pop()));
            }
        } else { // not found
            stack.pop();
            toLiteral();
            if (stack.iPop() != 0) { // is number
                if (state == COMPILE) {
                    comma(litWord.xt());
                    comma(stack.pop());
                }
            } else {
                // no word no literal, print string and exit
                String what = dictionary.findString(stack.pop());
                if (!Util.isEmpty(what)) {
                    print(what + "?");
                }
            }
        }
    }

    @Override
    protected void _dump() {
        int count = stack.pop();
        int start = stack.pop();
        for (int n = 0; n < count; n++) {
            int current = start + n;
            if (current % 8 == 0) {
                print("\n");
                print(formatNumber(current));
                print(" ");
            }
            print(formatNumber(cFetch(current)));
            print(" ");
        }
        print("\n");
    }

    @Override
    public int cFetch(int address) {
        if (getMemoryMapper().isBufferAddress(address)) {
            return blockBuffer.cfetch(address);
        }
        return super.cFetch(address);
    }

    @Override
    public void cStore(int address, int value) {
        if (getMemoryMapper().isBufferAddress(address)) {
            blockBuffer.cStore(address, value);
        }
        super.cStore(address, value);
    }

    /**
     * Opens a file for reading.
     *
     * @param path
     *                 name and path of the file
     */
    protected void _include(String path) {
        File file = new File(path);
        try {
            fis = new FileInputStream(file);
            blk = -1;
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface GetInt {
        int get();
    }

    @Override
    protected String parse(CheckChar check) {
        GetInt source = getSource();
        StringBuffer result = new StringBuffer("");

        // skip whitespace (and \0 characters)
        int zch = source.get();
        while (zch == 0 || zch > 0 && Character.isWhitespace(zch)) {
            zch = source.get();
        }

        // read to the next delimiter
        while (zch > 0 && !check.isDelimiter((char) zch)) {
            result.append((char) zch);
            zch = source.get();
        }
        return result.toString();
    }

    private GetInt getSource() {
        if (blk < 0) {
            return this::getCharFromFileInputStream;
        }
        if (blk > 0) {
            return this::getCharFromBlock;
        }
        return this::getCharFromTib;
    }

    /**
     * Reads a char from the terminal input buffer.
     *
     * @return the char read or -1
     */
    protected int getCharFromTib() {
        if (toIn < tibWord.length()) {
            return tibWord.charAt(toIn++);
        }
        return -1;
    }

    /**
     * Reads a character from the file inputstream if available.
     *
     * @return character read
     */
    protected int getCharFromFileInputStream() {
        int result = 0;
        try {
            result = fis.read();
        } catch (IOException e) {
            e.printStackTrace();
            result = -1;
        }
        if (result == -1) {
            blk = 0;
            Util.closeSilently(fis);
            fis = null;
        }
        return result;
    }

    /**
     * Reads a char from the block buffer into {@code blk}.
     *
     * @return the fetched value or -1
     */
    protected int getCharFromBlock() {
        if (toIn < BlockBuffer.BLOCK_SIZE) {
            return blockBuffer.cfetch(blk, toIn++);
        }
        blk = 0;
        return -1;
    }

    /**
     * 7.6.2.1770 LIST ( u -- )
     * <p>
     * Display block u in an implementation-defined format. Store u in SCR.
     */
    protected void _list() {
        scr = stack.iPop();
        print("\n\r");
        byte[] content = blockBuffer.blockContent(scr);
        for (int row = 0; row < 16; row++) {
            StringBuffer line = new StringBuffer(80);
            line.append(row).append(": ");
            for (int col = 0; col < 64; col++) {
                byte zch = content[64 * row + col];
                line.append(zch == '\0' ? ' ' : (char) zch);
            }
            print(line.append("\n\r").toString());
        }
    }

    /**
     * Creates and adds a new internal word to the engine's dictionary.
     *
     * @param name
     *                    name of the word
     * @param command
     *                    command to execute
     * @return the created and added {@link Word}-Object
     */
    private Word addF(String name, Command<Forth83Engine> command) {
        return super.add(name, command);
    }

    /**
     * EXPECT ( addr +n -- ) M,83.
     *
     * Receive characters and store each into memory. The transfer begins at addr
     * proceeding towards higher addresses one byte per character until either a
     * "return" is received or until +n characters have been transferred. No more
     * than +n characters will be stored. The "return" is not stored into memory. No
     * characters are received or transferred if +n is zero. All characters actually
     * received and stored into memory will be displayed, with the "return"
     * displaying as a space..<br>
     * The number of characters read is stored in "span".
     */
    protected void _expect() {
        int count = stack.iPop();
        int addr = stack.iPop();
        span = 0;
        while (span < count) {
            keyWord.cfa.execute(this);
            char zch = stack.cPop();
            if (zch == '\n') {
                break;
            } else if (zch == 8) {
                dictionary.cStore(addr + span, (int) zch);
                if (span > 0) {
                    printChar.accept(zch);
                    span--;
                }
                continue;
            } else if (zch < 32) {
                continue;
            }
            span++;
            dictionary.cStore(addr + span, (int) zch);
            printChar.accept(zch);
        }
    }
}
