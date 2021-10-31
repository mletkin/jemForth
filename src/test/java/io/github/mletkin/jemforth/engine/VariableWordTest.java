package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VariableWordTest {

    private VariableWord word = new VariableWord();

    @Test
    void executePushesPfa() {
        word.xt = 10;
        JemEngine engine = new JemEngine();
        word.execute(engine);
        assertThat(engine.getDataStack()).contains(14);
    }

    @Test
    void storeAndFetchManipulateTheValue() {
        word.store(1, 4711);
        assertThat(word.fetch(1)).isEqualTo(4711);
    }

}
