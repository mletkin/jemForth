package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.f83.Forth83Engine;
import io.github.mletkin.jemforth.engine.harness.Program;
import io.github.mletkin.jemforth.engine.testcases.Stack;

/**
 * Return stack manipulation.
 * <p>
 * The return stack can only be manipulated within words otherwise the engine breaks.<br>
 * Words like RP! can't be tested this way at all, because clearing the return stack stops the interpreter entirely.
 */
public class ReturnStackTest {

    @ParameterizedTest
    @MethodSource
    public void returnStackTest(Program prg) {
        fixture(new Forth83Engine()).test(prg);
    }

    public static Stream<Program> returnStackTest() {
        return Stack.testCasesReturnStack();
    }

}
