package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;
import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.f83.Forth83Engine;

public class AgainTest {

    /**
     * multiple LEAVEs in _one_ BEGIN..AGEIN loop
     */
    @Disabled
    @Test
    public void beginAgain() {
        fixture(new Forth83Engine()).test(program("") //
                .add(line("10 CONSTANT MAX_LEAVES")) //
                .add(line("CREATE _LEAVES MAX_LEAVES CELLS ALLOT")) //
                .add(line(
                        ": FIRSTFREE _LEAVES 0 BEGIN 2DUP CELLS + @ 0= IF CELLS + EXIT THEN 1+ DUP MAX_LEAVES = UNTIL ABORT ;")) //
                .add(line(": >LEAVES FIRSTFREE ! ;")) //
                .add(line(": LEAVE COMPILE BRANCH HERE >LEAVES 0 , ; IMMEDIATE")) //
                .add(line(
                        ": RESOLVE_ONE DUP CELLS _LEAVES + @ ?DUP IF SWAP CELLS _LEAVES + 0 SWAP ! ! ELSE 2DROP THEN ;")) //
                .add(line(
                        ": RESOLVE_ALL >R _LEAVES 0 BEGIN R@ OVER RESOLVE_ONE 1+ DUP MAX_LEAVES = UNTIL RDROP 2DROP ;")) //
                .add(line(": AGAIN  COMPILE BRANCH , HERE RESOLVE_ALL ; IMMEDIATE")) //

                .add(line(": cnt 0 BEGIN 1 + DUP 5 = IF LEAVE ELSE DUP . THEN AGAIN ;")) //
                .add(line("cnt").output("1 2 3 4 ")));
    }
}
