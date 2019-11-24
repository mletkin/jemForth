package io.github.mletkin.jemforth.engine.testcases;

import static io.github.mletkin.jemforth.engine.harness.Line.line;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.harness.Fixture;
import io.github.mletkin.jemforth.engine.harness.Line;

public class Math {

    public static Stream<Line> testCasesDouble() {
        return Fixture.testCaseList( //
                line("1000000000000. D.").output("1000000000000 "), //
                line("1000000000000. 1000000000000. D+ D.").output("2000000000000 "), //
    
                line("1000000000000. DNEGATE D.").output("-1000000000000 "), //
                line("-1000000000000. DNEGATE D.").output("1000000000000 "), //
                line("0. DNEGATE D.").output("0 "), //
    
                line("10000000000000. D2/ D.").output("5000000000000 "), //
                line("-10000000000000. D2/ D.").output("-5000000000000 "), //
                line("0. D2/ D.").output("0 "), //
    
                line("10000000000000. D2* D.").output("20000000000000 "), //
                line("-10000000000000. D2* D.").output("-20000000000000 "), //
                line("0. D2* D.").output("0 "), //
    
                line("1000000000000. 1000000000000. D< .").output("0 "), //
                line("1000000000000. 2000000000000. D< .").output("-1 "), //
                line("2000000000000. 1000000000000. D< .").output("0 "), //
    
                line("1000000000000. 1000000000000. D= .").output("-1 "), //
                line("1000000000000. 2000000000000. D= .").output("0 "), //
                line("2000000000000. 1000000000000. D= .").output("0 "), //
                line("-1. -1. D= .").output("-1 "), //
                line("0. 0. D= .").output("-1 "), //
    
                line("1000000000000.  D0< .").output("0 "), //
                line("-1000000000000. D0< .").output("-1 "), //
                line("0. D0< .").output("0 "), //
    
                line("10000000000000. DABS D.").output("10000000000000 "), //
                line("-10000000000000. DABS D.").output("10000000000000 "), //
                line("0. DABS D.").output("0 "), //
    
                line(" 1000000000000.  2000000000000. DMAX D.").output("2000000000000 "), //
                line(" 2000000000000.  1000000000000. DMAX D.").output("2000000000000 "), //
                line("-1000000000000. -2000000000000. DMAX D.").output("-1000000000000 "), //
                line("-2000000000000. -1000000000000. DMAX D.").output("-1000000000000 "), //
                line(" 1000000000000.  2000000000000. DMIN D.").output("1000000000000 "), //
                line(" 2000000000000.  1000000000000. DMIN D.").output("1000000000000 "), //
                line("-1000000000000. -2000000000000. DMIN D.").output("-2000000000000 "), //
                line("-2000000000000. -1000000000000. DMIN D.").output("-2000000000000 "), //
    
                line("4711. D>S .").output("4711 "), //
                line("-4711. D>S .").output("-4711 "), //
                line("0. D>S .").output("0 "), //
                line("20000 200000 UM* U.").output("4000000000 ")
        );
    }

    public static Stream<Line> testCasesInteger() {
        return Fixture.testCaseList( //
                line(" 10 20 +").stack(30), //
                line(" 10 20 -").stack(-10), //
                line(" 10 20 *").stack(200), //
                line(" 30 20 10 * +").stack(230), //
                line(" 20 10 /").stack(2), //
                line(" 10  3 /").stack(3), //
                line("-10 -4 /").stack(2), //
                line(" 10 5 MOD").stack(0), //
                line(" 12 5 MOD").stack(2), //
                line(" 10 1+").stack(11), //
                line(" -4 1+").stack(-3), //
                line(" 10 1-").stack(9), //
                line(" -4 1-").stack(-5), //
    
                line("  0 NEGATE").stack(0), //
                line("  5 NEGATE").stack(-5), //
                line(" -5 NEGATE").stack(5), //
    
                line("  0 INVERT").stack(-1), //
                line(" -1 INVERT").stack(0), //
    
                line(" 14 7 AND").stack(6), //
                line(" 14 7 OR").stack(15), //
                line(" 14 7 OR").stack(15), //
                line(" 10 6 XOR").stack(12), //
                line(" 10 6 XOR").stack(12), //
                line("  0 NOT").stack(-1), //
                line("  1 NOT").stack(-2), //
                line("  0 2*").stack(0), //
                line("  1 2*").stack(2), //
                line("  2 2*").stack(4), //
                line("  0 2/").stack(0), //
                line("  2 2/").stack(1), //
                line("  4 2/").stack(2), //
                line("10000000 10000 50000 */").stack(2000000), //
                line("20 10 MAX").stack(20), //
                line("10 20 MAX").stack(20), //
                line("20 10 MIN").stack(10), //
                line("10 20 MIN").stack(10), //
                line(" 10 ABS").stack(10), //
                line("-10 ABS").stack(10), //
                line("  0 ABS").stack(0), //
    
                line("47 6 /MOD").stack(5, 7), //
                line("7 8 3 */MOD").stack(18, 2), //
    
                line("0 INVERT").stack(-1), //
                line("-1 INVERT").stack(0),
    
                line("0. 1 UM/MOD").stack(0, 0), //
                line("1. 1 UM/MOD").stack(0, 1), //
                line("1. 2 UM/MOD").stack(1, 0), //
                line("3. 2 UM/MOD").stack(1, 1)//
    
        // Arguments.of("20000 200000 UM* U.", "4000000000 ", stack())
    
        );
    }

}
