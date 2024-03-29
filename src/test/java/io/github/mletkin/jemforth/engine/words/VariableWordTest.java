package io.github.mletkin.jemforth.engine.words;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.JemEngine;
import io.github.mletkin.jemforth.engine.TestUtils;
import io.github.mletkin.jemforth.engine.words.VariableWord;

class VariableWordTest {

    private VariableWord word = new VariableWord("foobar");
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
