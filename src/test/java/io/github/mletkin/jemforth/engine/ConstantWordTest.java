package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConstantWordTest {

    private ConstantWord word = new ConstantWord(4711);
    private JemEngine engine = TestUtils.mkEngineAddWord(word);

    @Test
    void executePushesTheValue() {
        word.execute(engine);
        assertThat(engine.getDataStack().stream()).contains(4711);
    }

    @Test
    void fetchGetsTheValue() {
        assertThat(word.fetch(15)).isEqualTo(4711);
    }

}
