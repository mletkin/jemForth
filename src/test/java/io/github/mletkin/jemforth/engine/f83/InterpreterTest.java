package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.f83.Forth83Engine;
import io.github.mletkin.jemforth.engine.harness.Program;
import io.github.mletkin.jemforth.engine.testcases.Interpreter;

/**
 * Test outer interpreter functions
 */
public class InterpreterTest {

    @ParameterizedTest
    @MethodSource
    public void interpreterTest(Program prg) {
        fixture(new Forth83Engine()).test(prg);
    }

    public static Stream<Program> interpreterTest() {
        return Interpreter.testCases();
    }

}
