package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;
import static io.github.mletkin.jemforth.engine.harness.StackExpression.constant;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.JemEngine;
import io.github.mletkin.jemforth.engine.f83.Forth83Engine;
import io.github.mletkin.jemforth.engine.harness.Fixture;
import io.github.mletkin.jemforth.engine.harness.Program;

/**
 * test WORD and FIND to find words :-)
 */
public class DictionaryTest {

    Fixture<JemEngine> fixture = Fixture.fixture(new Forth83Engine());

    @Test
    public void findWord() {
        fixture.test(Program.program("find word") //
                .add(line(": suche BL WORD FIND ;")) //
                .add(line("suche DUP ").stack(//
                        e -> e.getDictionary().find("DUP").xt, //
                        constant(-1) //
        )));
    }

    @Test
    public void findNonImmediateWord() {
        fixture.test(//
                line("'!' PAD 1+ C! PAD FIND").stack(//
                        e -> e.getDictionary().find("!").xt, //
                        constant(-1)//
                ));
    }

    @Test
    public void findImmediateWord() {
        fixture.test(//
                line("';' PAD 1+ C! PAD FIND").stack(//
                        e -> e.getDictionary().find(";").xt, //
                        constant(1)//
                ));
    }

    @Test
    public void wontFindUnknownWord() {
        fixture.test(program("wont find unknown word") //
                .add(line("SP!").check(e -> assertThat(e.getDictionary().find("§")).isNull()))
                .add(line("'§' PAD 1+ C! PAD FIND")//
                        .stack(e -> e.getDictionary().find("PAD").xt + 1, //
                                constant(0)) //
        ));
    }

    @Test
    public void wontFindForgottenWord() {
        fixture.test(program("wont find forgotten word") //
                .add(line(": § ;")) //
                .add(line("FORGET §")//
                        .check(e -> assertThat(e.getDictionary().find("§")).isNull())) //
                .add(line("'§' PAD 1+ C! PAD FIND").stack(//
                        e -> e.getDictionary().find("PAD").xt + 1, //
                        constant(0))));

    }

    /**
     * 6.1.2450 WORD 2012 standard tests
     */
    @Test
    public void word2012TestWithBlanc() {
        fixture.test(program("")//
                .add(line(": GS3 WORD COUNT SWAP C@ ;")) //
                .add(line("BL GS3 HELLO").stack(5, (int) 'H')) //
        );
    }

    @Test
    public void word2012TestWithQuote() {
        fixture.test(program("")//
                .add(line(": GS3 WORD COUNT SWAP C@ ;")) //
                .add(line("34 GS3 GOODBYE\"").stack(7, (int) 'G'))//
        );
    }

    @Disabled
    @Test
    public void word2012TestWithNewline() {
        fixture.test(program("")//
                .add(line(": GS3 WORD COUNT SWAP C@ ;")) //
                .add(line("BL GS3 \r\n" + "   DROP").stack(0)) //
        );
    }

}
