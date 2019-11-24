package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.f83.Forth83Engine;
import io.github.mletkin.jemforth.engine.harness.Line;
import io.github.mletkin.jemforth.engine.testcases.Output;

public class OutputTest {

    @ParameterizedTest
    @MethodSource
    public void outputTest(Line line) {
        fixture(new Forth83Engine()).test(line);
    }

    public static Stream<Line> outputTest() {
        return Output.testCases();
    }

}
