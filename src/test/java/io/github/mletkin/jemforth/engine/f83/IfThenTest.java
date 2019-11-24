package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.f83.Forth83Engine;
import io.github.mletkin.jemforth.engine.harness.Program;
import io.github.mletkin.jemforth.engine.testcases.IfThen;

public class IfThenTest {

    @ParameterizedTest
    @MethodSource
    public void ifThenTest(Program prg) {
        fixture(new Forth83Engine()).test(prg);
    }

    public static Stream<Program> ifThenTest() {
        return IfThen.testCases();
    }

    // @Test
    // public void beginUntilMitDot() {
    // engine.process(": cnt 10 1 0 begin over + >R 1 + over R> = until ;");
    // checkResult(10, process("cnt"));
    // }

}
