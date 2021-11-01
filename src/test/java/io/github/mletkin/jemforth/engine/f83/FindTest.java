package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;
import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;
import static io.github.mletkin.jemforth.engine.harness.StackExpression.constant;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.harness.Fixture;
import io.github.mletkin.jemforth.engine.harness.Program;

public class FindTest {

    private Fixture fixture = fixture(new Forth83Engine());

    @Test
    public void findWord() {
        Program prg = program("defined word is found") //
                .add(line(": suche BL WORD FIND ;")) //
                .add(line("suche DUP")//
                        .stack(//
                                e -> e.getDictionary().find("DUP").xt, //
                                constant(-1)));
        fixture.test(prg);
    }

    @Test
    public void wontFindForgottenWord() {
        Program prg = program("forgotten word is ot found") //
                .add(line(": suche BL WORD FIND ;")) //
                .add(line(": x ;").check(e -> assertThat(e.getDictionary().find("x")).isNotNull())) //
                .add(line("FORGET x").check(e -> assertThat(e.getDictionary().find("x")).isNull())) //
                .add(line("suche x")//
                        .stack(//
                                e -> e.getDictionary().find("wordBuffer").xt + 1, //
                                constant(0)));
        fixture.test(prg);
    }

    // Program prg = program("") //
    // .add(Line.of(": x ;")) //
    //
    // .add(Xline2.of("FORGET x")//
    // .check(e -> assertThat(e).isNull()) //
    // .check(e -> assertThat(e.getDictionary().find("x")).isNull())) //
    //
    // .add(Xline2.of("'x' PAD 1+ C! PAD FIND").stack(//
    // e -> e.getDictionary().find("PAD").xt + 1, //
    // e -> 0) //
    // );
    //
    // Fixture.of(engine).test(prg);
}
