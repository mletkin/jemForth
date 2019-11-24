package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Fixture.testCaseList;
import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Program;

public class IfThen {

    public static Stream<Program> testCases() {
        return testCaseList( //
                program("ifThen") //
                        .add(line(": jump 0= IF .\" ja \" THEN .\" doch\" ;")) //
                        .add(line("0 jump").output("ja doch")) //
                        .add(line("1 jump").output("doch")),
    
                program("ifThenWithMarkResolve") //
                        .add(line(": IF     COMPILE ?BRANCH  >MARK ; IMMEDIATE")) //
                        .add(line(": THEN   >RESOLVE ; IMMEDIATE")) // a
                        .add(line(": jump 0= IF .\" ja \" THEN .\" doch\" ;")) //
                        .add(line("0 jump").output("ja doch")) //
                        .add(line("1 jump").output("doch")),
    
                program("ifThenElse") //
                        .add(line(": jump 0= IF .\" ja \" ELSE .\" nein \" THEN .\" doch\" ;")) //
                        .add(line("0 jump").output("ja doch")) //
                        .add(line("1 jump").output("nein doch")),
    
                program("ifThenElseWithMarkResolve") //
                        .add(line(": IF COMPILE ?BRANCH >MARK ; IMMEDIATE")) //
                        .add(line(": THEN >RESOLVE ; IMMEDIATE")) //
                        .add(line(": ELSE COMPILE BRANCH >MARK SWAP >RESOLVE ; IMMEDIATE")) //
                        .add(line(": jump 0= IF .\" ja \" THEN .\" doch\" ;")) //
                        .add(line("0 jump").output("ja doch")) //
                        .add(line("1 jump").output("doch")) //
        );
    }

    public static Stream<Program> testCasesLoop() {
        return testCaseList( //
                program("DO LOOP") //
                        .add(line(": sum 0 16 0 DO 1+ LOOP ;")) //
                        .add(line("sum").stack(16)),
    
                program("DO +Loop up") //
                        .add(line(": COUNTDOWN 100 10 DO I . 10 +LOOP ;")) //
                        .add(line("COUNTDOWN").output("10 20 30 40 50 60 70 80 90 ")),
    
                program("DO +LOOP down") //
                        .add(line(": COUNTDOWN 0 100 DO I . -10 +LOOP ;")) //
                        .add(line("COUNTDOWN").output("100 90 80 70 60 50 40 30 20 10 ")),
    
                program("DO LOOP with I") //
                        .add(line(": sum 0 16 0 DO I + LOOP ;")) //
                        .add(line("sum").stack(120)),
    
                program("BEGIN UNTIL") //
                        .add(line(": cnt 0 BEGIN 1 + DUP 5 = UNTIL ;")) //
                        .add(line("cnt").stack(5)),
    
                program("REPEAT with LEAVE") //
                        .add(line(": LOOPY BEGIN 1+ DUP 10 > IF LEAVE THEN DUP . REPEAT ;")) //
                        .add(line("5 LOOPY").output("6 7 8 9 10 ").stack(11)),
    
                program("WHILE REPEAT") //
                        .add(line(": bar BEGIN DUP . 1- ?DUP WHILE '*' EMIT REPEAT ;")) //
                        .add(line("5 bar").output("5 *4 *3 *2 *1 ")) //
        );
    }

}
