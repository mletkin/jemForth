package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.exception.IllegalMemoryAccessException;

class CellListWordTest {

    private CellListWord word = new CellListWord();

    @Test
    void executePushesPfa() {
        word.xt = 10;
        JemEngine engine = new JemEngine();
        word.execute(engine);
        assertThat(engine.getDataStack()).contains(14);
    }

    @Test
    void fetchGetsCorrectParameter() {
        word.xt = 10 << 16;
        word.addPfaEntry(4711);
        word.addPfaEntry(4712);
        word.addPfaEntry(4713);

        assertThat(word.fetch(word.xt + 4)).isEqualTo(4711);
        assertThat(word.fetch(word.xt + 8)).isEqualTo(4712);
        assertThat(word.fetch(word.xt + 12)).isEqualTo(4713);
    }

    @Test
    void storeSetsARandomCell() {
        word.xt = 10 << 16;
        word.addPfaEntry(4711);
        word.addPfaEntry(4712);
        word.addPfaEntry(4713);

        word.store(word.xt + 8, 5555);

        assertThat(word.getDataArea()).containsExactly(4711, 5555, 4713);
    }

    @Test
    void storeAfterTheLastFilledCellAllocatesMemory() {
        word.xt = 10 << 16;
        word.addPfaEntry(4711);

        word.store(word.xt + 8, 5555);

        assertThat(word.getDataArea()).containsExactly(4711, 5555);
    }

    @Test
    void storeAfterTheLastFilledCellAllocatesMemory_5() {
        word.xt = 10 << 16;
        word.addPfaEntry(4711);

        word.store(word.xt + 20, 5555);

        assertThat(word.getDataArea()).containsExactly(4711, null, null, null, 5555);
    }

    @Test
    void storeWithAddressTooSmallThrowsException() {
        word.xt = 10 << 16;
        assertThatExceptionOfType(IllegalMemoryAccessException.class).isThrownBy(() -> word.store(word.xt, 5555));
    }

    @Test
    void storeWithAddressTooBigThrowsException() {
        word.xt = 10 << 16;
        assertThatExceptionOfType(IllegalMemoryAccessException.class)
                .isThrownBy(() -> word.store(word.xt + 65536, 5555));
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
        word.xt = 10 << 16;
        word.addPfaEntry(0xA0B0C0D);

        assertThat(word.cFetch(word.xt + 4 + 0)).isEqualTo(13);
        assertThat(word.cFetch(word.xt + 4 + 1)).isEqualTo(12);
        assertThat(word.cFetch(word.xt + 4 + 2)).isEqualTo(11);
        assertThat(word.cFetch(word.xt + 4 + 3)).isEqualTo(10);
    }

    @Test
    void cFetchOnNullGetsZero() {
        word.xt = 10 << 16;
        word.addPfaEntry(null);

        assertThat(word.cFetch(word.xt + 4 + 0)).isEqualTo(0);
        assertThat(word.cFetch(word.xt + 4 + 1)).isEqualTo(0);
        assertThat(word.cFetch(word.xt + 4 + 2)).isEqualTo(0);
        assertThat(word.cFetch(word.xt + 4 + 3)).isEqualTo(0);
    }

    @Test
    void cStoreSetsTheBytes() {
        word.xt = 10 << 16;
        word.addPfaEntry(0);
        word.cStore(word.xt + 4 + 0, 13);
        word.cStore(word.xt + 4 + 1, 12);
        word.cStore(word.xt + 4 + 2, 11);
        word.cStore(word.xt + 4 + 3, 10);

        assertThat(word.getDataArea()).containsExactly(0xA0B0C0D);
    }

    void cStorehOnNullSetsTheByte() {
        word.xt = 10 << 16;
        word.addPfaEntry(null);

        word.cStore(word.xt + 4 + 0, 13);

        assertThat(word.fetch(word.xt + 4)).isEqualTo(13);
    }
}
