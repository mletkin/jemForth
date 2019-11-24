package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Fixture;
import io.github.mletkin.jemforth.engine.harness.Line;

public class InternalVaraibles {

    public static Stream<Line> testCases() {
        return Fixture.testCaseList( //
                line("STATE @").stack(0), //
                line("DECIMAL HEX BASE @").stack(16), //
                line("HEX DECIMAL BASE @").stack(10), //
                line("HEX OCTAL BASE @").stack(8), //
                line("HEX BIN BASE @").stack(2), //
    
                line("[").check(e -> assertThat(e.getState()).as("State ist not interpret").isEqualTo(0)), //
                line("]").check(e -> assertThat(e.getState()).as("State ist not compile").isEqualTo(-1)) //
        );
    }

}
