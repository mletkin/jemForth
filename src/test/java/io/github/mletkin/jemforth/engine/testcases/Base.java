package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Fixture.testCaseList;
import static io.github.mletkin.jemforth.engine.harness.Line.line;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Line;

public class Base {

    public static Stream<Line> testCases() {
        return testCaseList(//
                line("CHAR (").stack(40), //
                line("CHAR xyz").stack(120), //
                line("10 ( 20 30 ) 20").stack(10, 20), //
                line("10 ( 20 30 20").stack(10), //
                line("10 ( 20 ( 30 ) 20").stack(10, 20), //
                line("CHAR"));
    }

}
