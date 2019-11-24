package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Fixture.testCaseList;
import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Program;

public class Compile {

    public static Stream<Program> testCases() {
        return testCaseList( //
                program("bracketCharSingleCharacter")//
                        .add(line(": GC1 [CHAR] X ;")) //
                        .add(line("GC1").stack(88)),
    
                program("bracketCharMultiCharacter") //
                        .add(line(": GC2 [CHAR] HELLO ;")) //
                        .add(line("GC2").stack(72)),
    
                program("bracketTickBracket") // 2012 std
                        .add(line(": GT1 123 ;")) //
                        .add(line(": GT2 ['] GT1 ;")) //
                        .add(line("GT2 EXECUTE").stack(123)), //
    
                program("bracketCompileBracketNonImmediate") //
                        .add(line(": [c1] [COMPILE] DUP ; IMMEDIATE")) //
                        .add(line("123 [c1]").stack(123, 123)),
    
                program("bracketCompileBracketImmediate") //
                        .add(line(": [c1] [COMPILE] DUP ; IMMEDIATE")) //
                        .add(line(": [c2] [COMPILE] [c1] ;")) //
                        .add(line("234 [c2]").stack(234, 234)),
    
                program("bracketCompileBracketSpecialSemantics") //
                        .add(line(": [cif] [COMPILE] IF ; IMMEDIATE")) //
                        .add(line(": [c3]  [cif] 111 ELSE 222 THEN ;")) //
                        .add(line("-1 [c3]").stack(111)) //
                        .add(line("0 [c3]").stack(111, 222)),
    
                program("compileLiteral")//
                        .add(line("4711 : foo LITERAL ; foo").stack(4711)),
    
                program("toBodyTest") //
                        .add(line("CREATE CR0")) //
                        .add(line("' CR0 >BODY HERE = ").stack(-1)), //
    
                program("tick").ref("6.1.0070") //
                        .add(line(": GT1 123 ;")) //
                        .add(line("' GT1 EXECUTE").stack(123)) //
    
        );
    }

}
