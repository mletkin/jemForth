package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Fixture.testCaseList;
import static io.github.mletkin.jemforth.engine.harness.Line.line;
import static io.github.mletkin.jemforth.engine.harness.Program.program;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Fixture;
import io.github.mletkin.jemforth.engine.harness.Line;
import io.github.mletkin.jemforth.engine.harness.Program;

public class Stack {

    public static Stream<Program> testCasesReturnStack() {
        return Fixture.testCaseList( //
                program(">R and R>") //
                        .add(line(": n 10 >R .RSTACK R> ;")) //
                        .add(line("n").stack(10).outputEndsWith("10")),
    
                program("R@") //
                        .add(line(": n 10 >R R@ .RSTACK R> ;")) //
                        .add(line("n").stack(10, 10).outputEndsWith(" 10")),
    
                program("RDROP") //
                        .add(line(": n 10 20 >R >R RDROP .RSTACK R> ;")) //
                        .add(line("n").stack(20).outputEndsWith(" 20")));
    }

    public static Stream<Line> testCasesDataStack() {
        return testCaseList( //
                line("10 DUP").stack(10, 10), //
                line("10 ?DUP").stack(10, 10), //
                line("0 ?DUP").stack(0), //
                line("10 20 DROP").stack(10), //
                line("3 5 10 SWAP").stack(3, 10, 5), //
                line("1 2 OVER").stack(1, 2, 1), //
                line("1 2 3 4 0 PICK").stack(1, 2, 3, 4, 4), //
                line("1 2 3 4 1 PICK").stack(1, 2, 3, 4, 3), //
                line("1 2 3 4 3 PICK").stack(1, 2, 3, 4, 1), //
                line("1 2 3 ROT").stack(2, 3, 1), //
                line("1 2 3 4 5 3 ROLL").stack(1, 3, 4, 5, 2), //
                line("DEPTH").stack(0), //
                line("1 2 DEPTH").stack(1, 2, 2), //
    
                line("10 20 30 SP!").stack(), //
                // double value stack manipulation
                line("1 2 2DUP").stack(1, 2, 1, 2), //
                line("1 2 3 4 2SWAP").stack(3, 4, 1, 2), //
                line("1 2 3 4 2OVER").stack(1, 2, 3, 4, 1, 2), //
                line("1 2 3 4 5 6 2ROT").stack(3, 4, 5, 6, 1, 2), //
                line("1 2 3 4 2DROP").stack(1, 2), //
    
                line("  0 S>D").stack(0, 0), //
                line("  1 S>D").stack(1, 0), //
                line("  2 S>D").stack(2, 0), //
                line(" -1 S>D").stack(-1, -1), //
                line(" -2 S>D").stack(-2, -1), //
    
                line("1 2 NIP").stack(2), //
                line("1 2 TUCK").stack(2, 1, 2) //
        );
    }

}
