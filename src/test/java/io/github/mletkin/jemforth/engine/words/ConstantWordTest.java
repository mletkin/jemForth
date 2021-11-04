package io.github.mletkin.jemforth.engine.words;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.JemEngine;
import io.github.mletkin.jemforth.engine.TestUtils;
import io.github.mletkin.jemforth.engine.words.ConstantWord;

class ConstantWordTest {

    private ConstantWord word = new ConstantWord("foobar", 4711);
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
