package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.harness.Line;
import io.github.mletkin.jemforth.engine.testcases.InternalVariables;

public class InternalVariableTest {

    @ParameterizedTest
    @MethodSource
    public void internalVariableTest(Line line) {
        fixture(new Forth83Engine()).test(line);
    }

    public static Stream<Line> internalVariableTest() {
        return InternalVariables.testCases();
    }

}
