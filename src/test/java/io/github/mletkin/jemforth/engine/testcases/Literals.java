package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Fixture;
import io.github.mletkin.jemforth.engine.harness.Program;

public class Literals {

    public static Stream<Program> testCases() {
        return Fixture.testCaseList( //
                program("hex number").add(line("DECIMAL BASE @ $FF").stack(10, 255)), //
                program("binary number").add(line("DECIMAL BASE @ %1111").stack(10, 15)), //
                program("character").add(line("DECIMAL BASE @ '@'").stack(10, 64)), //
                program("decimal number").add(line("HEX BASE @ #4711").stack(16, 4711)), //
                program("tick with more than one character").add(line("'xy'").output("'xy'?")), //
                program("not a number").add(line("pipapo").output("pipapo?"))//
        );
    }

}
