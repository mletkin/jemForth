package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Fixture.testCaseList;
import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Program;

public class Word {

    public static Stream<Program> testCases() {
        return testCaseList(//
                program("constant word").add(line("10 CONSTANT pi 20 pi").stack(20, 10)), //
                program("constant as colon").add(line("20 : ten 10 ; ten").stack(20, 10)), //
                program("execute colon with two words").add(line("20 : quad DUP DUP + + ; 30 quad").stack(20, 90)), //
                program("use colon in colon").add(line("20 : ten 10 ; : plusten ten + ; plusten").stack(30)), //
                program("define, store and fetch variable").add(line("VARIABLE foo 10 foo ! 20 foo @").stack(20, 10)), //
                program("recurse")//
                        .add(line(": FACT DUP 2 < IF DROP 1 EXIT THEN DUP 1- RECURSE * ;"))
                        .add(line("7 FACT").stack(5040)),

                program("use Constant in colon II") //
                        .add(line("20 10 CONSTANT foo").stack(20)) // define const
                        .add(line("foo").stack(20, 10)) // call const word
                        .add(line("DROP").stack(20)) // drop constant value
                        .add(line(": bar foo + ;").stack(20)) // define word with constant
                        .add(line("bar").stack(30)) //
        );
    }

}
