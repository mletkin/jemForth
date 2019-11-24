package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Fixture;
import io.github.mletkin.jemforth.engine.harness.Program;

public class Interpreter {

    // parse a Word, try to find and execute it
   private static final java.lang.String GO = ": go BL WORD FIND RUN EXIT ; IMMEDIATE ";

    public static Stream<Program> testCases() {
        return Fixture.testCaseList( //
                program("execute of tick'ed word").add(line("20 ' DUP EXECUTE").stack(20, 20)), //
                program("execute run with literal").add(line(GO + "go 444").stack(444)), //
                program("execute run with normal word").add(line(GO + "go DEPTH").stack(0)), //

                program("execute run with unknown word").add(line(GO + "go $x$y$").output("$x$y$?")), //
                program("run within colon definition")//
                        .add(line(GO + " : bar go ( DUP ) go 47 go DUP ; SEE bar")
                                .output("\n: bar (LITERAL) 47 DUP EXIT ; ")));
    }

}
