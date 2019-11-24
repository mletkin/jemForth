package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Fixture.testCaseList;
import static io.github.mletkin.jemforth.engine.harness.Program.program;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Line;
import io.github.mletkin.jemforth.engine.harness.Program;

public class String {

    public static Stream<Program> testCases() {
        return testCaseList( //
    
                program("empty string") //
                        .add(Line.line("STRING a$ a$ COUNT TYPE").output("")),
    
                program("one character") //
                        .add(Line.line("STRING a$ 'a' C, a$ COUNT TYPE").output("a")),
    
                program("many character") //
                        .add(Line.line("STRING a$ 'a' C, 'b' C, 'c' C, 'd' C, a$ COUNT TYPE").output("abcd")),
    
                program("S-Quote").ref("6.1.2165") //
                        .add(Line.line(": s1 S\" abc\" ; s1 TYPE").output("abc")), //
    
                program("C-Quote").ref("6.2.0855") //
                        .add(Line.line(": s2 C\" xyz\" ; s2  COUNT TYPE").output("xyz")), //
    
                program("").ignore() //
                        .add(Line.line(": s8 S\" abc \" ; s8 -TRAILING s8 2 - =").stack(-1)) //
        );
    }

}
