package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.harness.Program;
import io.github.mletkin.jemforth.engine.testcases.IfThen;

public class LoopTest {

    @ParameterizedTest
    @MethodSource
    public void loopTest(Program prg) {
        fixture(new Forth83Engine()).test(prg);
    }

    private static Stream<Program> loopTest() {
        return IfThen.testCasesLoop();
    }

}
