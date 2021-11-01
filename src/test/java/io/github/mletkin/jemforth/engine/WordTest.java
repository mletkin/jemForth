package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.exception.NotByteAlignedException;
import io.github.mletkin.jemforth.engine.exception.NotCellAlignedException;

class WordTest {

    private Word word = new Word();
    private JemEngine engine = TestUtils.mkEngineAddWord(word);

    @Test
    void executePushesPfa() {
        word.execute(engine);
        assertThat(engine.getDataStack()).contains(word.xt() + 4);
    }

    @Test
    void newWordHasNoName() {
        assertThat(word.name()).isNull();
    }

    @Test
    void newWordIsNotImmidiate() {
        assertThat(word.isImmediate()).isFalse();
    }

    @Test
    void immidiateMakesImmediateTrue() {
        word.immediate();
        assertThat(word.isImmediate()).isTrue();
    }

    @Test
    void newWordHasNoComment() {
        assertThat(word.getComment()).isNull();
    }

    @Test
    void getCommentGetsComments() {
        word.comment("one", "two", "three");
        assertThat(word.getComment()).isEqualTo("one\ntwo\nthree");
    }

    @Test
    void addPfaEntryThrowsException() {
        assertThatExceptionOfType(NotCellAlignedException.class).isThrownBy(() -> word.addPfaEntry(1));
    }

    @Test
    void fetchThrowsException() {
        assertThatExceptionOfType(NotCellAlignedException.class).isThrownBy(() -> word.fetch(1));
    }

    @Test
    void cFetchThrowsException() {
        assertThatExceptionOfType(NotByteAlignedException.class).isThrownBy(() -> word.cFetch(1));
    }

    @Test
    void storeThrowsException() {
        assertThatExceptionOfType(NotCellAlignedException.class).isThrownBy(() -> word.store(1, 2));
    }

    @Test
    void cStoreThrowsException() {
        assertThatExceptionOfType(NotByteAlignedException.class).isThrownBy(() -> word.cStore(1, 2));
    }

    @Test
    void cellCountIsZero() {
        assertThat(word.cellCount()).isZero();
    }

    @Test
    void dataAreaIsEmpty() {
        assertThat(word.getDataArea()).isEmpty();
    }

    @Test
    void nameReturnsTheName() {
        word.name = "name";
        assertThat(word.name()).isEqualTo("name");
    }

    @Test
    void toStringContainsTheXt() {
        word.name = "name";
        assertThat(word.toString()).isEqualTo("name[" + word.xt() + "]");
    }

}
