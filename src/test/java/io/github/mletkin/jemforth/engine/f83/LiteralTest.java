package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.f83.Forth83Engine;
import io.github.mletkin.jemforth.engine.harness.Program;
import io.github.mletkin.jemforth.engine.testcases.Literals;

/**
 * Test the parsing of literals
 */
public class LiteralTest {

    @ParameterizedTest
    @MethodSource
    public void numberParsingTest(Program prg) {
        fixture(new Forth83Engine()).test(prg);
    }

    public static Stream<Program> numberParsingTest() {
        return Literals.testCases();
    }
}
