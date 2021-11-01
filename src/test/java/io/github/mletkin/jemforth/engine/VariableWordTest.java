package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VariableWordTest {

    private VariableWord word = new VariableWord();
    private JemEngine engine = TestUtils.mkEngineAddWord(word);

    @Test
    void executePushesPfa() {
        engine.add(word);
        word.execute(engine);

        assertThat(engine.getDataStack()).contains(word.xt() + 4);
    }

    @Test
    void storeAndFetchManipulateTheValue() {
        word.store(1, 4711);
        assertThat(word.fetch(1)).isEqualTo(4711);
    }

}
