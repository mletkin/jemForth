package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConstantWordTest {

    private ConstantWord word = new ConstantWord(4711);

    @Test
    void fetchGetsTheValue() {
        assertThat(word.fetch(15)).isEqualTo(4711);
    }

    @Test
    void executePushesTheValue() {
        JemEngine engine = new JemEngine();
        word.execute(engine);

        assertThat(engine.getDataStack().stream()).contains(4711);
    }
}
