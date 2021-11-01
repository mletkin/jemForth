package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;
import static io.github.mletkin.jemforth.engine.harness.Fixture.testCaseList;
import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.harness.Program;

public class DoesToTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource
    public void doesToTest(Program prg) {
        fixture(new Forth83Engine()).test(prg);
    }

    public static Stream<Program> doesToTest() {
        return testCaseList( //
                program("define constant with does>") //
                        .add(line(": CONSTANT_F CREATE , DOES> @ ;")) //
                        .add(line("70 CONSTANT_F foobar")) //
                        .add(line("foobar").stack(70)), //

                program("define and access array") //
                        .add(line(": ARRAY CREATE CELLS ALLOT DOES> SWAP CELLS + ;")) //
                        .add(line("2 ARRAY B")) //
                        .add(line("44 0 B !")) //
                        .add(line("66 1 B !")) //
                        .add(line("0 B @ 1 B @").stack(44, 66)) //
        );
    }
}
