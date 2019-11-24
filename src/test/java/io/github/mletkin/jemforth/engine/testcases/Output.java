package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Fixture.testCaseList;
import static io.github.mletkin.jemforth.engine.harness.Line.line;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Line;

public class Output {

    public static Stream<Line> testCases() {
        return testCaseList( //
                line("42 EMIT").output("*"), //
                line("4711 .").output("4711 "), //
                line("255 HEX .").output("FF "), //
                line("65535 HEX .").output("FFFF "), //
                line("CR").output("\r\n"), //
                line("10 20 30 .S").output("10 20 30").stack(10, 20, 30), //
                line("SPACE").output(" "), //
                line("0 SPACES").output(""), //
                line("5 SPACES").output("     "), //
                line(".\" white rabbit\"").output("white rabbit"), //
                line(": bf .\" brown fox\" ; bf").output("brown fox"), //
                line(".( white rabbit)").output("white rabbit"), //
                line(": bf .( white rabbit) ;").output("white rabbit") //
        );
    }

}
