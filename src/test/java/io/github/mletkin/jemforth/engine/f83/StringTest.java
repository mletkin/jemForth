package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.harness.Fixture;
import io.github.mletkin.jemforth.engine.harness.Line;
import io.github.mletkin.jemforth.engine.harness.Program;
import io.github.mletkin.jemforth.engine.testcases.String;

/**
 * TODO: check, if blanc after String ist intended outputPipe
 */
public class StringTest {

    Fixture<Forth83Engine> fixture = fixture(new Forth83Engine());

    @ParameterizedTest
    @MethodSource
    public void stringDefinitionTest(Program prg) {
        fixture.test(prg);
    }

    public static Stream<Program> stringDefinitionTest() {
        return String.testCases();
    }

    @Test
    public void trailingEmpty() {
        fixture.execute("STRING a$ a$");
        int aAdr = fixture.engine().getDataStack().iPeek(0);
        fixture.test(Line.line("COUNT -TRAILING ").stack(aAdr + 1, 0));
    }

    @Test
    public void trailingBlancOnly() {
        fixture.execute("STRING a$ ' ' C, ' ' C, a$");
        int aAdr = fixture.engine().getDataStack().iPeek(0);
        fixture.test(Line.line("COUNT -TRAILING ").stack(aAdr + 1, 0));
    }

    @Test
    public void trailingSome() {
        fixture.execute("STRING a$ ' ' C, 'a' C, 'b' C, 'c' C, ' ' C, ' ' C, a$");
        int aAdr = fixture.engine().getDataStack().iPeek(0);
        fixture.test(Line.line("COUNT -TRAILING ").stack(aAdr + 1, 4));
    }

}
