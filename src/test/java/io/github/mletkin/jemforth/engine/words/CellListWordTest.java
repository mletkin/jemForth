package io.github.mletkin.jemforth.engine.words;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.JemEngine;
import io.github.mletkin.jemforth.engine.TestUtils;
import io.github.mletkin.jemforth.engine.exception.IllegalMemoryAccessException;
import io.github.mletkin.jemforth.engine.words.CellListWord;

class CellListWordTest {

    private CellListWord word = new CellListWord("foobar");
    private JemEngine engine = TestUtils.mkEngineAddWord(word);

    @Test
    void executePushesPfa() {
        word.execute(engine);
        assertThat(engine.getDataStack()).contains(word.xt() + 4);
    }

    @Test
    void fetchGetsCorrectParameter() {
        word.addPfaEntry(4711);
        word.addPfaEntry(4712);
        word.addPfaEntry(4713);

        assertThat(word.fetch(word.xt() + 4)).isEqualTo(4711);
        assertThat(word.fetch(word.xt() + 8)).isEqualTo(4712);
        assertThat(word.fetch(word.xt() + 12)).isEqualTo(4713);
    }

    @Test
    void storeSetsARandomCell() {
        word.addPfaEntry(4711);
        word.addPfaEntry(4712);
        word.addPfaEntry(4713);

        word.store(word.xt() + 8, 5555);

        assertThat(word.getDataArea()).containsExactly(4711, 5555, 4713);
    }

    @Test
    void storeAfterTheLastFilledCellAllocatesMemory() {
        word.addPfaEntry(4711);

        word.store(word.xt() + 8, 5555);

        assertThat(word.getDataArea()).containsExactly(4711, 5555);
    }

    @Test
    void storeAfterTheLastFilledCellAllocatesMemory_5() {
        word.addPfaEntry(4711);

        word.store(word.xt() + 20, 5555);

        assertThat(word.getDataArea()).containsExactly(4711, null, null, null, 5555);
    }

    @Test
    void storeWithAddressTooSmallThrowsException() {
        assertThatExceptionOfType(IllegalMemoryAccessException.class).isThrownBy(() -> word.store(word.xt(), 5555));
    }

    @Test
    void storeWithAddressTooBigThrowsException() {
        assertThatExceptionOfType(IllegalMemoryAccessException.class)
                .isThrownBy(() -> word.store(word.xt() + 65536, 5555));
    }

    @Test
    void addPfaEntryAddsParameter() {
        word.addPfaEntry(4711);
        word.addPfaEntry(4712);

        assertThat(word.getDataArea()).containsExactly(4711, 4712);
    }

    @Test
    void cellCountReturnsParameterListSize() {
        word.addPfaEntry(4711);
        word.addPfaEntry(4712);

        assertThat(word.cellCount()).isEqualTo(2);
    }

    @Test
    void cFetchGetsTheBytes() {
        word.addPfaEntry(0xA0B0C0D);

        assertThat(word.cFetch(word.xt() + 4 + 0)).isEqualTo(13);
        assertThat(word.cFetch(word.xt() + 4 + 1)).isEqualTo(12);
        assertThat(word.cFetch(word.xt() + 4 + 2)).isEqualTo(11);
        assertThat(word.cFetch(word.xt() + 4 + 3)).isEqualTo(10);
    }

    @Test
    void cFetchOnNullGetsZero() {
        word.addPfaEntry(null);

        assertThat(word.cFetch(word.xt() + 4 + 0)).isEqualTo(0);
        assertThat(word.cFetch(word.xt() + 4 + 1)).isEqualTo(0);
        assertThat(word.cFetch(word.xt() + 4 + 2)).isEqualTo(0);
        assertThat(word.cFetch(word.xt() + 4 + 3)).isEqualTo(0);
    }

    @Test
    void cStoreSetsTheBytes() {
        word.addPfaEntry(0);
        word.cStore(word.xt() + 4 + 0, 13);
        word.cStore(word.xt() + 4 + 1, 12);
        word.cStore(word.xt() + 4 + 2, 11);
        word.cStore(word.xt() + 4 + 3, 10);

        assertThat(word.getDataArea()).containsExactly(0xA0B0C0D);
    }

    void cStorehOnNullSetsTheByte() {
        word.addPfaEntry(null);

        word.cStore(word.xt() + 4, 13);

        assertThat(word.fetch(word.xt() + 4)).isEqualTo(13);
    }
}
