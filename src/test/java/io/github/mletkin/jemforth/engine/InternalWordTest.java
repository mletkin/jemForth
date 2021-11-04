package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.words.InternalWord;

class InternalWordTest {

    @Test
    void nameParameterSetsTheName() {
        InternalWord word = new InternalWord("foobar", Command.NOP);
        assertThat(word.name()).isEqualTo("foobar");
    }

    @Test
    void cfaParameterSetsTheCfa() {
        Store<Integer> store = new Store<>();
        InternalWord word = new InternalWord("foobar", e -> store.set(10));
        JemEngine engine = TestUtils.mkEngineAddWord(word);

        word.execute(engine);

        assertThat(store.get()).isEqualTo(10);
    }

}
