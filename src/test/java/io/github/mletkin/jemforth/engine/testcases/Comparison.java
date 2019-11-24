package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Fixture.testCaseList;
import static io.github.mletkin.jemforth.engine.harness.Line.line;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Line;

public class Comparison {

    public static Stream<Line> testCases() {
        return testCaseList(//
                line("CHAR (").stack(40), //
                line("10 20 =").stack(0), //
                line("10 10 =").stack(-1), //
                line("20 10 =").stack(0), //
    
                line(" 10 20 <").stack(-1), //
                line(" 10 10 <").stack(0), //
                line(" 20 10 <").stack(0), //
    
                line(" 10 20 >").stack(0), //
                line(" 10 10 >").stack(0), //
                line(" 20 10 >").stack(-1), //
    
                line("  0  0=").stack(-1), //
                line("  1  0=").stack(0), //
                line("  2  0=").stack(0), //
                line(" -1  0=").stack(0), //
    
                line("-10  0<").stack(-1), //
                line("  0  0<").stack(0), //
                line(" 10  0<").stack(0), //
    
                line("-10  0>").stack(0), //
                line("  0  0>").stack(0), //
                line(" 10  0>").stack(-1), //
    
                line("10 20 <>").stack(-1), //
                line("10 10 <>").stack(0), //
                line("20 10 <>").stack(-1), //
    
                line("10 20 <=").stack(-1), //
                line("10 10 <=").stack(-1), //
                line("20 10 <=").stack(0), //
    
                line("10 20 >=").stack(0), //
                line("10 10 >=").stack(-1), //
                line("20 10 >=").stack(-1), //
    
                line("FALSE").stack(0), //
                line("TRUE").stack(-1) //
        );
    }

}
