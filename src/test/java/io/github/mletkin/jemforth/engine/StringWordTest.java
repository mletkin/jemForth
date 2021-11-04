package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assumptions.assumeThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.exception.IllegalStringLengthException;
import io.github.mletkin.jemforth.engine.words.StringWord;

public class StringWordTest {

    private StringWord word = new StringWord("");
    private JemEngine engine = TestUtils.mkEngineAddWord(word);

    @Test
    void executePushesPfa() {
        word.setData("foobar");
        word.execute(engine);
        assertThat(engine.getDataStack()).contains(word.xt() + 1);
    }

    @Test
    void basicFetch() {
        word.setData("ABCDE");
        assertThat(word.cFetch(word.xt() + 5)).isEqualTo((int) 'D');
        assertThat(word.cFetch(word.xt() + 1)).isEqualTo(5);
    }

    @Test
    void fetchWithEmptyString() {
        word.setData("");
        assertThat(word.cFetch(word.xt() + 4)).isEqualTo(0);
        assertThat(word.cFetch(word.xt() + 1)).isEqualTo(0);
    }

    @Test
    void fetchWithNull() {
        word.setData(null);
        assertThat(word.cFetch(word.xt() + 103)).isEqualTo(0);
        assertThat(word.cFetch(word.xt() + 0)).isEqualTo(0);
    }

    @Test
    void basicStore() {
        word.setData("ABCDE");
        word.cStore(word.xt() + 5, (int) 'X');
        assertThat(word.data()).isEqualTo("ABCXE");
        assertThat(word.cFetch(word.xt() + 5)).isEqualTo((int) 'X');
        assertThat(word.cFetch(word.xt() + 1)).isEqualTo(5);
    }

    @Test
    void storeWithEmptyString() {
        word.setData("");
        word.cStore(4, (int) 'X');
        assertThat(word.data()).isEqualTo("  X");
        assertThat(word.cFetch(word.xt() + 1)).isEqualTo(3);
    }

    @Test
    void storeWithNull() {
        word.setData(null);
        word.cStore(word.xt() + 4, (int) 'X');
        assertThat(word.data()).isEqualTo("  X");
        assertThat(word.cFetch(word.xt() + 1)).isEqualTo(3);
    }

    @Test
    void storeWithStringTooShortAllocatesSpace() {
        word.setData("ABC");
        word.cStore(word.xt() + 7, (int) 'X');
        assertThat(word.data()).isEqualTo("ABC  X");
        assertThat(word.cFetch(word.xt() + 1)).isEqualTo(6);
    }

    @Test
    void clearSetsContentToNullString() {
        word.setData("foobar");
        assumeThat(word.data()).isEqualTo("foobar");
        word.clear();
        assertThat(word.data()).isEmpty();
    }

    @Test
    void appenCharAppendsChar() {
        word.setData("foobar");
        assumeThat(word.data()).isEqualTo("foobar");
        word.append('X');
        assertThat(word.data()).isEqualTo("foobarX");
    }

    @Test
    void prependCharPreendsChar() {
        word.setData("foobar");
        assumeThat(word.data()).isEqualTo("foobar");
        word.prepend('X');
        assertThat(word.data()).isEqualTo("Xfoobar");
    }

    @Test
    void allotFillsWithBlancs() {
        word.setData("foobar");
        word.allot(5);
        assertThat(word.data()).isEqualTo("foobar     ");
    }

    @Test
    void allotZeroChangesNothing() {
        word.setData("foobar");
        word.allot(0);
        assertThat(word.data()).isEqualTo("foobar");
    }

    @Test
    void allotNegativeChangesNothing() {
        word.setData("foobar");
        word.allot(-2);
        assertThat(word.data()).isEqualTo("foobar");
    }

    @Test
    void increasingLengthByteAllocatesMemory() {
        word.setData("foobar");
        word.cStore(word.xt() + 1, 10);
        assertThat(word.data()).isEqualTo("foobar    ");
    }

    @Test
    void reducingLengthByteShortensContent() {
        word.setData("foobar");
        word.cStore(word.xt() + 1, 3);
        assertThat(word.data()).isEqualTo("foo");
    }

    @Test
    void settingLengthByteToZeroClearsData() {
        word.setData("foobar");
        word.cStore(word.xt() + 1, 0);
        assertThat(word.data()).isEmpty();
    }

    @Test
    void settingLengthByteToNegativeValueThrowsException() {
        word.setData("foobar");
        assertThatExceptionOfType(IllegalStringLengthException.class).isThrownBy(() -> word.cStore(word.xt() + 1, -1));
    }

    @Test
    void settingLengthByteToooHighThrowsException() {
        word.setData("foobar");
        assertThatExceptionOfType(IllegalStringLengthException.class)
                .isThrownBy(() -> word.cStore(word.xt() + 1, 2 << 17));
    }

    @Test
    void toStringContainsXt() {
        word.setData("foobar");
        assertThat(word.toString()).isEqualTo("foobar[" + word.xt() + "]");
    }

    @Test
    void addPfaEntryThrowsNoException() {
        assertThatNoException().isThrownBy(() -> word.addPfaEntry(4711));
    }

}
