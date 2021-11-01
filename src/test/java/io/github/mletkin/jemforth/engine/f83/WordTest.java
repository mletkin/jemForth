package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.harness.Program;
import io.github.mletkin.jemforth.engine.testcases.Word;

public class WordTest {

    @ParameterizedTest
    @MethodSource
    public void wordTest(Program prg) {
        fixture(new Forth83Engine()).test(prg);
    }

    public static Stream<Program> wordTest() {
        return Word.testCases();
    }

    @Test
    public void useConstInColon() {
    }

    // @Test
    // public void compileWithTick() {
    // checkResult(400, process("20 : square ' DUP ' * [ , , ] ; square"));
    // }

}
