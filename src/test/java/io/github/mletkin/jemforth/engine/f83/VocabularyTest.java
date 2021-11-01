package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;
import static io.github.mletkin.jemforth.engine.harness.Fixture.testCaseList;
import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.harness.Program;

public class VocabularyTest {

    @ParameterizedTest
    @MethodSource
    public void vocabularyTest(Program prg) {
        fixture(new Forth83Engine()).test(prg);
    }

    public static Stream<Program> vocabularyTest() {
        return testCaseList( //
                program("twoVoacbularies") //
                        .add(line("VOCABULARY foo")) //
                        .add(line("foo DEFINITIONS")) //
                        .add(line(": bar 10 ;")) //
                        .add(line("VOCABULARY fii")) //
                        .add(line("fii DEFINITIONS")) //
                        .add(line(": bar 20 ;")) //
                        .add(line("foo bar").stack(10)) //
                        .add(line("SP!")) //
                        .add(line("fii bar").stack(20)),

                program("forthIsDefault") //
                        .add(line(": bar 10 ;")) //
                        .add(line("VOCABULARY foo")) //
                        .add(line("foo DEFINITIONS")) //
                        .add(line(": bar 20 ;"))

                        .add(line("bar").stack(20)) //
                        .add(line("SP!")) //
                        .add(line("FORTH")) //
                        .add(line("bar").stack(10)) //

        );
    }

}
