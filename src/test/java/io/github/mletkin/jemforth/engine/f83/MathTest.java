package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.f83.Forth83Engine;
import io.github.mletkin.jemforth.engine.harness.Line;
import io.github.mletkin.jemforth.engine.testcases.Math;

public class MathTest {

    @ParameterizedTest
    @MethodSource
    public void mathDouble(Line line) {
        fixture(new Forth83Engine()).test(line);
    }

    public static Stream<Line> mathDouble() {
        return Math.testCasesDouble();
    }

    @ParameterizedTest
    @MethodSource
    public void mathInteger(Line line) {
        fixture(new Forth83Engine()).test(line);
    }

    public static Stream<Line> mathInteger() {
        return Math.testCasesInteger();
    }

//    assertOutput("HEX ff000000 ff000001 U< U.", "-1");

}
