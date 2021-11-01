package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;

import io.github.mletkin.jemforth.engine.f83.Forth83Engine;

public class EngineTest {

    JemEngine engine = new Forth83Engine();

    protected int process(String str) {
        engine.process(str);
        return engine.getDataStack().iPeek(0);
    }

    protected void assertStack(String str, int... argList) {
        engine.process(str);
        assertThat(engine.stack).hasSameSizeAs(argList);
        for (int n = argList.length - 1; n >= 0; n--) {
            assertThat(engine.stack.get(n).intValue()).isEqualTo(argList[n]);
        }
    }

    protected EngineTest assertStack(Integer... argList) {
        assertThat(engine.stack).hasSameSizeAs(argList);
        for (int n = argList.length - 1; n >= 0; n--) {
            assertThat(engine.stack.get(n)).isEqualTo(argList[n]);
        }
        return this;
    }

    protected void assertEmpty(String str) {
        engine.process(str);
        assertThat(engine.stack).as("Stack not empty").isEmpty();
    }

    protected void checkResult(int expected, int actual) {
        assertThat(actual).isEqualTo(expected);
    }

    protected void dumpStack(Inspectable engine) {
        String list = String.join(",", //
                engine.getDataStack().stream().map(i -> Integer.toString(i)).collect(Collectors.toList()));
        System.out.println("[" + list + "]");
    }

    protected void assertStackEmpty() {
        assertThat(engine.getDataStack()).isEmpty();
    }

}
