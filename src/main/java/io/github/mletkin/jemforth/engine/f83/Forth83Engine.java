package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.MemoryMapper.CELL_SIZE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;

import io.github.mletkin.jemforth.engine.Command;
import io.github.mletkin.jemforth.engine.ForthEngine;
import io.github.mletkin.jemforth.engine.InternalWord;
import io.github.mletkin.jemforth.engine.JemEngine;
import io.github.mletkin.jemforth.engine.MemoryMapper;
import io.github.mletkin.jemforth.engine.StringWord;
import io.github.mletkin.jemforth.engine.UserVariableWord;
import io.github.mletkin.jemforth.engine.Util;
import io.github.mletkin.jemforth.engine.Word;
import io.github.mletkin.jemforth.engine.exception.ForthTerminatedException;
import io.github.mletkin.jemforth.engine.exception.JemForthException;

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

    // will be changed to the "real" eecutor later during construction
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
        add("DUP", ForthEngine.DUP).comment(ForthEngine.DUP_C);
        add("DROP", ForthEngine.DROP).comment(ForthEngine.DROP_C);
        add("SWAP", ForthEngine.SWAP).comment(ForthEngine.SWAP_C);
        add("OVER", ForthEngine.OVER).comment(ForthEngine.OVER_C);
        add("ROT", ForthEngine.ROT).comment(ForthEngine.ROT_C);

        add("?DUP", ForthEngine.Q_DUP).comment(ForthEngine.Q_DUP_C);
        add("DEPTH", ForthEngine.DEPTH).comment(ForthEngine.DEPTH_C);
        add("PICK", ForthEngine.PICK).comment(ForthEngine.PICK_C);
        add("ROLL", ForthEngine.ROLL).comment(ForthEngine.ROLL_C);

        // Compilation / Dictionary / Definition
        add(":", ForthEngine.COLON).comment(ForthEngine.COLON_C);
        add(";", ForthEngine.SEMICOLON).immediate().comment(ForthEngine.SEMICOLON_C);
        add("'", ForthEngine.TICK).comment(ForthEngine.TICK_C);
        add("IMMEDIATE", ForthEngine.IMMEDIATE).immediate().comment(ForthEngine.IMMEDIATE_C);

        addF("COMPILE", Forth83Engine::_compile);
        add("[COMPILE]", ForthEngine.POSTPONE).immediate().comment(ForthEngine.POSTPONE_C);

        add("CREATE", ForthEngine.CREATE).comment(ForthEngine.CREATE_C);
        add("ALLOT", ForthEngine.ALLOT).comment(ForthEngine.ALLOT_C);
        add("VARIABLE", ForthEngine.VARIABLE).comment(ForthEngine.VARIABLE_C);
        add("CONSTANT", ForthEngine.CONSTANT).comment(ForthEngine.CONSTANT_C);
        add("WORD", ForthEngine.WORD).comment(ForthEngine.WORD_C);
        add("FIND", ForthEngine.FIND).comment(ForthEngine.FIND_C);
        add(">BODY", ForthEngine.TO_BODY).comment(ForthEngine.TO_BODY_C);

        add("!", ForthEngine.STORE).comment(ForthEngine.STORE_C);
        add("@", ForthEngine.FETCH).comment(ForthEngine.FETCH_C);

        add(new InternalWord("C!", ForthEngine.C_STORE).comment(ForthEngine.C_STORE_C));
        add(new InternalWord("C@", ForthEngine.C_FETCH).comment(ForthEngine.C_FETCH_C));

        // Vocabularies are specific for Forth 83 and not part of the ANS-Standard
        // The FENCE/FORGET mechanism are legacy words in 2012
        add(dictionary.fenceWord("FENCE"));
        add("FORGET", ForthEngine.FORGET).comment(ForthEngine.FORGET_C);
        add(new UserVariableWord("CONTEXT", //
                () -> dictionary.getSearchResolver().getOrder().findFirst().orElse(null), //
                v -> dictionary.getSearchResolver().setOrder(v)));
        add(new UserVariableWord("CURRENT", //
                () -> dictionary.getSearchResolver().getCurrent(), //
                v -> dictionary.getSearchResolver().setCurrent(v)));
        addF("VOCABULARY", c -> c.dictionary.add(c.dictionary.getSearchResolver().createVocabulary(c.parseName())))
                .comment("( <name> -- )", "create a vocabulary with the given name");
        add(": DEFINITIONS CONTEXT @ CURRENT ! ;");

        add(": BUFFER: CREATE ALLOT ;").comment(ForthEngine.BUFFER_COLON_C);

        // logic operators

        add("AND", ForthEngine.BIT_AND).comment(ForthEngine.BIT_AND_C);
        add("OR", ForthEngine.BIT_OR).comment(ForthEngine.BIT_OR_C);
        add("XOR", ForthEngine.BIT_XOR).comment(ForthEngine.BIT_XOR_C);
        add("INVERT", ForthEngine.BIT_INVERT).comment(ForthEngine.BIT_INVERT_C);
        add(": FALSE 0 ; "); // 6.2.1485
        add(": TRUE 0 INVERT ; "); // 6.2.2298

        // NOT is not standardized.
        // F79 prefers 1's complement, while 2012 prefers 2's complement
        // The use in if..then constructs depends on the implementation of TRUE as 1 or
        // -1
        // We use biswise inversion here
        add("NOT", ForthEngine.BIT_INVERT).comment(ForthEngine.BIT_INVERT_C);

        // comparison operators
        add("=", ForthEngine.CMP_EQ).comment(ForthEngine.CMP_EQ_C);
        add("<", ForthEngine.CMP_LT).comment(ForthEngine.CMP_LT_C);
        add(">", ForthEngine.CMP_GT).comment(ForthEngine.CMP_GT_C);

        add("0=", ForthEngine.CMP_0_EQ).comment(ForthEngine.CMP_0_EQ_C);
        add("0<", ForthEngine.CMP_0_LT).comment(ForthEngine.CMP_0_LT_C);
        add("0>", ForthEngine.CMP_0_GT).comment(ForthEngine.CMP_0_GT_C);
        add("0<>", ForthEngine.CMP_0_NE).comment(ForthEngine.CMP_0_NE_C);

        add(": <> = NOT ;").comment(ForthEngine.CMP_NE_C);
        add(": <= > NOT ;");
        add(": >= < NOT ;");

        // signed single cell math operations
        add("+", ForthEngine.PLUS).comment(ForthEngine.PLUS_C);
        add("*", ForthEngine.TIMES).comment(ForthEngine.TIMES_C);
        add("-", ForthEngine.MINUS).comment(ForthEngine.MINUS_C);
        add("/", ForthEngine.DIV).comment(ForthEngine.DIV_C);
        add("MOD", ForthEngine.MOD).comment(ForthEngine.MOD_C);

        add(": 1+ 1 + ;");// 6.1.0290
        add(": 1- 1 - ;");// 6.1.0300
        add(": 2+ 2 + ;");
        add(": 2- 2 - ;");

        add(",", ForthEngine.COMMA).comment(ForthEngine.COMMA_C);
        add("C,", ForthEngine.C_COMMA).comment(ForthEngine.C_COMMA_C);

        // return stack access
        add(">R", JemEngine.TO_R).comment(JemEngine.TO_R_C);
        add("R>", JemEngine.R_FROM).comment(JemEngine.R_FROM_C);
        add("R@", JemEngine.R_FETCH).comment(JemEngine.R_FETCH_C);
        add("RDROP", JemEngine.R_DROP).comment(JemEngine.R_DROP_C);
        add("RP!", JemEngine.R_CLEAR).comment(JemEngine.R_CLEAR_C);
        add(".RSTACK", JemEngine.DOT_RSTACK).comment(JemEngine.DOT_RSTACK_C);

        addF("I", c -> c.rPeek(2));// 6.1.1680
        addF("J", c -> c.rPeek(4));// 6.1.1730
        addF("K", c -> c.rPeek(6));// non std

        add(": CELLS " + CELL_SIZE + " * ;"); // 6.1.0890
        add(": CELL+ " + CELL_SIZE + " + ;"); // 6.1.0880
        // 6.1.0897 CHAR+
        // 6.1.0898 CHARS

        // double cell stack manipulation
        add(": 2DUP OVER OVER ;").comment("( n1 n2 -- n1 n2 n1 n2 )");// 6.1.03380
        add(": 2DROP DROP DROP ;").comment("( n1 n2 -- )");// 6.1.0370
        add(": 2SWAP ROT >R ROT R> ;").comment("( n1 n2 n3 n4 -- n3 n4 n1 n2 )");// 6.1.0430
        add(": 2OVER >R >R 2DUP R> R> 2SWAP ;").comment(ForthEngine.TWO_OVER_C);// 6.1.0400
        add(": 2ROT >R >R 2SWAP R> R> 2SWAP ;");

        add(": 2>R SWAP >R >R ;") // 6.2.0340
                .comment("( x1 x2 -- ) ( R: -- x1 x2 )", "Transfer cell pair x1 x2 to the return stack.");
        add(": 2R> R> R> SWAP ;") // 6.2.0410
                .comment("( -- x1 x2 ) ( R: x1 x2 -- ) ", "Transfer cell pair x1 x2 from the return stack.");
        add(": 2R@ R> R> 2DUP >R >R SWAP ;") // 6.2.0415
                .comment("( -- x1 x2 ) ( R: x1 x2 -- x1 x2 )", "Copy x1 x2 from the return stack.");

        add(new UserVariableWord("DP", () -> dictionary.getHereValue(), READ_ONLY));
        add(": HERE DP @ ;").comment(ForthEngine.HERE_C);

        // words for flow control
        add(": BEGIN  HERE ; IMMEDIATE");// 6.1.0760
        add(": UNTIL  COMPILE ?BRANCH , ; IMMEDIATE"); // 6.1.2390
        add(": END    [COMPILE] UNTIL ; IMMEDIATE");// alias for until
        add(": LEAVE  COMPILE BRANCH HERE SWAP 0 , ; IMMEDIATE");// 6.1.1760
        add(": AGAIN  COMPILE BRANCH , ; IMMEDIATE"); // 6.2.0700

        add(": WHILE  COMPILE ?BRANCH HERE 0 , ; IMMEDIATE");// 6.1.2430
        add(": REPEAT COMPILE BRANCH  HERE CELL+ SWAP ! , ; IMMEDIATE");// 6.1.2140

        add(": IF     COMPILE ?BRANCH HERE 0 , ; IMMEDIATE");// 6.1.1700
        add(": THEN   HERE SWAP ! ; IMMEDIATE");// 6.1.2270
        add(": ELSE   COMPILE BRANCH  HERE 0 , SWAP HERE SWAP ! ; IMMEDIATE");// 6.1.1310

        add(": DO HERE COMPILE >R COMPILE >R ; IMMEDIATE");// 6.1.1240
        add(": LOOP COMPILE R> COMPILE R> COMPILE 1+  COMPILE 2DUP COMPILE = COMPILE ?BRANCH , COMPILE 2DROP ; IMMEDIATE");// 6.1.1800
        add(": +LOOP COMPILE R> COMPILE R> COMPILE ROT COMPILE + COMPILE 2DUP COMPILE = COMPILE ?BRANCH , COMPILE 2DROP ; IMMEDIATE"); // 6.1.0140

        add(": >MARK HERE 0 , ;");
        add(": >RESOLVE HERE SWAP ! ;");

        add(": <MARK HERE ;");
        add(": <RESOLVE , ;");

        add("RECURSE", ForthEngine.RECURSE).immediate().comment(ForthEngine.RECURSE_C);

        // I/O
        add("EMIT", ForthEngine.EMIT).comment(ForthEngine.EMIT_C);
        add(": COUNT 1+ DUP 1- C@ ;").comment("( a1 -- a2 u )", // 6.1.0980
                "Push length and address of first char for the string address on the stack.");

        add(": TYPE ?DUP IF OVER + SWAP DO I C@ EMIT LOOP ELSE DROP THEN ;") // 6.1.2310
                .comment("( addr len -- )", "print String with address of first char and length");

        addF(">STRING", Forth83Engine::copyToString); // non-Standard
        add(": C\" $22 WORD >STRING COMPILE (STRLITERAL) , ; IMMEDIATE"); // 6.2.0855
        add(": .\" STATE @ IF [COMPILE] C\" COMPILE COUNT COMPILE TYPE ELSE $22 WORD COUNT TYPE THEN ; IMMEDIATE");
        add(": S\" $22 WORD >STRING COUNT SWAP COMPILE (STRLITERAL) , COMPILE (LITERAL) , ; IMMEDIATE"); // 6.1.2165

        // single precision integer math
        add("*/", ForthEngine.TIMES_DIVIDE).comment(ForthEngine.TIMES_DIVIDE_C);
        add("*/MOD", ForthEngine.TIMES_DIVIDE_MOD).comment(ForthEngine.TIMES_DIVIDE_MOD_C);
        add("/MOD", ForthEngine.SLASH_MOD).comment(ForthEngine.SLASH_MOD_C);
        add("2*", ForthEngine.TWO_TIMES).comment(ForthEngine.TWO_TIMES_C);
        add("2/", ForthEngine.TWO_DIV).comment(ForthEngine.TWO_DIV_C);
        add("ABS", ForthEngine.ABS).comment(ForthEngine.ABS_C);
        add("MAX", ForthEngine.MAX).comment(ForthEngine.MAX_C);
        add("MIN", ForthEngine.MIN).comment(ForthEngine.MIN_C);
        add("NEGATE", ForthEngine.NEGATE).comment(ForthEngine.NEGATE_C);
        add("LSHIFT", ForthEngine.LSHIFT).comment(ForthEngine.LSHIFT_C);
        add("RSHIFT", ForthEngine.RSHIFT).comment(ForthEngine.RSHIFT_C);
        // FM/MOD 6.1.1561
        // SM/REM 6.1.2214

        // double precision integer math
        add("DNEGATE", ForthEngine.D_NEGATE).comment(ForthEngine.D_NEGATE_C);
        add("D+", ForthEngine.D_PLUS).comment(ForthEngine.D_PLUS_C);
        add("D-", ForthEngine.D_MINUS).comment(ForthEngine.D_MINUS_C); // : D- DNEGATE D+ ;
        add("D0=", ForthEngine.D_CMP_0_EQ).comment(ForthEngine.D_CMP_0_EQ_C); // : D0= OR 0= ;
        add("D0<", ForthEngine.D_CMP_0_LT).comment(ForthEngine.D_CMP_0_LT_C);
        add("D=", ForthEngine.D_CMP_EQ).comment(ForthEngine.D_CMP_EQ_C); // : D= D- D0= ;
        add("D<", ForthEngine.D_CMP_LT).comment(ForthEngine.D_CMP_LT_C);
        add("D2*", ForthEngine.D_TWO_TIMES).comment(ForthEngine.D_TWO_TIMES_C);
        add("D2/", ForthEngine.D_TWO_DIV).comment(ForthEngine.D_TWO_TIMES_C);
        add("DABS", ForthEngine.D_ABS).comment(ForthEngine.D_ABS_C); // : DABS DUP 0< IF DNEGATE THEN ;

        add("DMAX", ForthEngine.D_MAX).comment(ForthEngine.D_MAX_C);
        add("DMIN", ForthEngine.D_MIN).comment(ForthEngine.D_MIN_C);
        add("D>S", ForthEngine.D_TO_S).comment(ForthEngine.D_TO_S_C);
        add("DU<", ForthEngine.D_CMP_U_LT).comment(ForthEngine.D_CMP_U_LT_C);

        add("M*", ForthEngine.M_TIMES).comment(ForthEngine.M_TIMES_C);
        // M*/ 94: 8.6.1.1820
        // M+ 94: 8.6.1.1830

        // unsigned integer math
        add("U<", ForthEngine.U_CMP_LT).comment(ForthEngine.U_CMP_LT_C);
        add("U>", ForthEngine.U_CMP_GT).comment(ForthEngine.U_CMP_GT_C);
        add("UM*", ForthEngine.UM_TIMES).comment(ForthEngine.UM_TIMES_C);
        add("UM/MOD", ForthEngine.UM_SLASH_MOD).comment(ForthEngine.UM_SLASH_MOD_C);

        // output/character/String
        add(": CR #10 #13 EMIT EMIT ;"); // 6.1.0990
        add(": BL 20 ;"); // 6.1.0770
        add(": SPACE BL EMIT ;"); // 6.1.2220
        add(": SPACES BEGIN DUP 0> WHILE SPACE 1- REPEAT DROP ;"); // 6.1.2230

        add(": .( 29 WORD COUNT TYPE ; IMMEDIATE"); // 6.2.0200

        add("FILL", Forth83Engine.FILL).comment(Forth83Engine.FILL_C);
        add(": BLANK BL FILL ;").comment("( addr u -- "); // 17.6.1.0780
        add(": ERASE 0 FILL ;").comment("( addr u -- "); // 6.2.1350
        add("CMOVE", ForthEngine.C_MOVE).comment(ForthEngine.C_MOVE_C);
        add("CMOVE>", ForthEngine.C_MOVE_UP).comment(ForthEngine.C_MOVE_UP_C);

        add(": +! DUP @ ROT + SWAP ! ;").comment("( n1 n2 -- )"); // 6.1.0130 not tested
        add(": 2! DUP >R ! R> CELL+ ! ;"); // 6.1.0310 not tested
        add(": 2@ DUP @ >R CELL+ @ R> ;"); // 6.1.0350 not tested

        add(": 2VARIABLE CREATE 2 CELLS ALLOT ;"); // 8.6.1.0440 not tested
        add(": 2CONSTANT CREATE , , DOES> 2@ ;"); // 8.6.1.0360 not tested
        // 2VALUE 8.6.2.0435

        keyWord = add("KEY", ForthEngine.KEY).comment(ForthEngine.KEY_C);
        add("KEY?", ForthEngine.KEY_Q).comment(ForthEngine.KEY_Q_C);
        add(": ( 29 WORD DROP ; IMMEDIATE"); // 6.1.0080
        add("HEX", ForthEngine.BASE_HEX).comment(ForthEngine.BASE_HEX_C);
        add("DECIMAL", ForthEngine.BASE_DECIMAL).comment(ForthEngine.BASE_DECIMAL_C);
        add("OCTAL", ForthEngine.BASE_OCTAL).comment(ForthEngine.BASE_OCTAL_C);
        add("BIN", ForthEngine.BASE_BIN).comment(ForthEngine.BASE_BIN_C);

        add("EXECUTE", ForthEngine.EXECUTE).comment(ForthEngine.EXECUTE_C);
        add(": LITERAL STATE @ IF COMPILE (LITERAL) , THEN ; IMMEDIATE"); // 6.1.1780 from figForth
        add("[", ForthEngine.BRACKET).immediate().comment(ForthEngine.BRACKET_C);
        add("]", ForthEngine.RIGHT_BRACKET).comment(ForthEngine.RIGHT_BRACKET_C);

        addF(">LITERAL", Forth83Engine::toLiteral);

        add("[']", ForthEngine.BRACKET_TICK_BRACKET).immediate().comment(ForthEngine.BRACKET_TICK_BRACKET_C);

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

        add("S>D", ForthEngine.S_TO_D).comment(ForthEngine.S_TO_D_C);// not_83_std
        add("U->D", c -> stack.dPush(stack.uPop())).comment("( u -- d )", "extend unsigned to double");// not_83_std
        add(": .R >R S>D R> D.R ;"); // 6.2.0210

        add(": D. SWAP OVER DABS " //
                + "<# #S SIGN #> " //
                + "TYPE SPACE ;").comment("( d -- )", "Print double followed by a space");// 8.6.1.1060
        add(": .  S>D D. ;").comment(ForthEngine.DOT_C);
        add(": U. U->D D. ;").comment("( u -- )", "Print unsigned integer followed by a space");// 6.1.2620

        add(": U.R >R U->D R> D.R ;")// 6.2.2330
                .comment("( u n -- )", "Print unsigned right aligned in a field n characters wide.");

        // not in F83 standard
        add(": DLITERAL STATE @ IF SWAP [COMPILE] LITERAL " + // figForth 79
                "[COMPILE] LITERAL THEN ; IMMEDIATE");

        add(": \\ BEGIN KEY DUP 0 = SWAP 10 = OR UNTIL ; IMMEDIATE"); // 6.2.2230
        add("STRING", ForthEngine.CREATE_STRING).comment(ForthEngine.CREATE_STRING_C);

        add(".S", ForthEngine.DOT_S).comment(ForthEngine.DOT_S_C);

        addF("SP!", c -> c.stack.clear());

        add("CHAR", ForthEngine.CHAR).comment(ForthEngine.CHAR_C);
        add("[CHAR]", ForthEngine.BRACKET_CHAR).immediate().comment(ForthEngine.BRACKET_CHAR_C);

        add("SEE", ForthEngine.SEE).comment(ForthEngine.SEE_C);
        add("SEEE", Forth83Engine.SEEE).comment(ForthEngine.SEEE_C);
        add("WORDS", ForthEngine.WORDS).comment(ForthEngine.WORDS_C);
        add("DUMP", Forth83Engine.DUMP).comment(Forth83Engine.DUMP_C);

        add(": ? @ . ;").comment(ForthEngine.Q_C);

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

        Word intWord = add(": INTERPRET BEGIN BLK @ >IN @ #TIB @ < OR WHILE BL WORD FIND RUN REPEAT ;")//
                .comment("( -- )", "Interpret all words from the current input stream");

        add(": LOAD BLK ! 0 >IN ! INTERPRET ;"); // 7.6.1.1790
        add(": THRU 1+ SWAP DO I LOAD LOOP ;"); // 7.6.2.2280
        add(": --> BLK DUP @ ?DUP IF 1 + SWAP ! LOAD ELSE DROP THEN ; IMMEDIATE")//
                .comment("( -- )", "load the next block");

        addF("INCLUDE", c -> c._include(c.parseName())).comment("( <name> -- )", "process the specified file");
        add("-TRAILING", ForthEngine.DASH_TRAILING).comment(ForthEngine.DASH_TRAILING_C);

        // span keeps the number of character read
        add(new UserVariableWord("SPAN", () -> span, v -> span = v));

        addF("EXPECT", Forth83Engine::_expect).comment("( addr n -- )");
        // fig : QUERY TIB @ 50 EXPECT 0 IN ! ;
        add(": QUERY TIB #80 EXPECT SPAN @ TIB C! 0 >IN ! ;")
                .comment("read max. 80 characters into the TIB and reset >IN");
        add(": QUIT [COMPILE] [ BEGIN RP! CR '>' EMIT QUERY BL EMIT INTERPRET STATE @ 0= IF .\" OK\" THEN AGAIN ;"); // 6.1.2050
        add(": ABORT SP! DECIMAL [COMPILE] FORTH DEFINITIONS QUIT ;"); // 6.1.0670
        add(": ABORT\" [COMPILE] IF [COMPILE] .\" COMPILE ABORT [COMPILE] THEN ; IMMEDIATE"); // 6.1.0680

        add("BYE", ForthEngine.BYE).comment(ForthEngine.BYE_C);

        add(": FORTH-83 ;");

        add("TIME", ForthEngine.TIME).comment(ForthEngine.TIME_C);
        add("VOCABULARY EDITOR"); // 15.6.2.1300

        // initialize engine
        add("#10 BASE !");
        add("' TIME FENCE !");

        // from here on we use the FORTH interpreter
        executor = c -> execute(intWord);
    }

    /**
     * create a new String word from the string whose address is on the stack.
     */
    private void copyToString() {
        StringWord source = (StringWord) dictionary.findWordContainingPfa(stack.pop());
        stack.push(dictionary.add(new StringWord("", source.data())).xt + 1);
    }

    @Override
    public void reset(boolean executionOnly) {
        super.reset(executionOnly);
        blk = 0;
    }

    // Copies the input string into the terminal input buffer and executes the
    // INTERPRET word.
    @Override
    public void process(String input) {
        tibWord.data(input);
        toIn = 0;
        executor.accept(this);
    }

    /**
     * Fill the terminal input buffer and execute the java coded interpreter.
     *
     * Used for boot strappping when the real INTERPRET word is not available
     * yet.<br>
     * This works for compiling certain colon definitions, but might cause problems,
     * when the execution involves words like INTERPRET.
     *
     * forth expression to execute
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
     * process the result of WORD and FIND.
     *
     * TODO: handle double literals properly
     */
    protected void doRun() {
        DUP.execute(this);
        if (!FIND_NOT.equals(stack.iPop())) { // if found
            if (state == COMPILE) { // if compile
                if (FIND_IMMEDIATE.equals(stack.iPop())) { // if immediate
                    execute(dictionary.findWordContainingPfa(stack.pop()));
                } else {
                    COMPILE_COMMA.execute(this);
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
                    comma(litWord.xt);
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
        if (MemoryMapper.isBufferAddress(address)) {
            return blockBuffer.cfetch(address);
        }
        return super.cFetch(address);
    }

    @Override
    public void cStore(int address, int value) {
        if (MemoryMapper.isBufferAddress(address)) {
            blockBuffer.cStore(address, value);
        }
        super.cStore(address, value);
    }

    private FileInputStream fis;
    private Word keyWord;

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

    protected int getCharFromTib() {
        if (toIn < tibWord.length()) {
            return tibWord.charAt(toIn++);
        }
        return -1;
    }

    /**
     * read a character from the fileinputstream if available.
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
     *
     */
    protected void _expect() {
        try {
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
        } catch (Exception e) {
            if (e instanceof IOException) {
                e.printStackTrace();
            } else {
                throw e;
            }
        }
    }
}
