package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StringWordTest extends EngineTest {

    StringWord word = new StringWord("");

    @BeforeEach
    void setup() {
        word.xt = 0;
    }

    @Test
    void basicFetch() {
        word.data("ABCDE");
        assertThat(word.cFetch(5)).isEqualTo((int) 'D');
        assertThat(word.cFetch(1)).isEqualTo(5);
    }

    @Test
    void fetchWithEmptyString() {
        word.data("");
        assertThat(word.cFetch(4)).isEqualTo(0);
        assertThat(word.cFetch(1)).isEqualTo(0);
    }

    @Test
    void fetchWithNull() {
        word.data(null);
        assertThat(word.cFetch(103)).isEqualTo(0);
        assertThat(word.cFetch(0)).isEqualTo(0);
    }

    @Test
    void basicStore() {
        word.data("ABCDE");
        word.cStore(5, (int) 'X');
        assertThat(word.data()).isEqualTo("ABCXE");
        assertThat(word.cFetch(5)).isEqualTo((int) 'X');
        assertThat(word.cFetch(1)).isEqualTo(5);
    }

    @Test
    void storeWithEmptyString() {
        word.data("");
        word.cStore(4, (int) 'X');
        assertThat(word.data()).isEqualTo("  X");
        assertThat(word.cFetch(1)).isEqualTo(3);
    }

    @Test
    void storeWithNull() {
        word.data(null);
        word.cStore(4, (int) 'X');
        assertThat(word.data()).isEqualTo("  X");
        assertThat(word.cFetch(1)).isEqualTo(3);
    }

    @Test
    void storeWithStringTooShort() {
        word.data("ABC");
        word.cStore(7, (int) 'X');
        assertThat(word.data()).isEqualTo("ABC  X");
        assertThat(word.cFetch(1)).isEqualTo(6);
    }

    @Test
    void clearSetsContentToNullString() {
        word.data("foobar");
        assumeThat(word.data()).isEqualTo("foobar");
        word.clear();
        assertThat(word.data()).isEmpty();
    }

    @Test
    void appenCharAppendsChar() {
        word.data("foobar");
        assumeThat(word.data()).isEqualTo("foobar");
        word.append('X');
        assertThat(word.data()).isEqualTo("foobarX");
    }

    @Test
    void prependCharPreendsChar() {
        word.data("foobar");
        assumeThat(word.data()).isEqualTo("foobar");
        word.prepend('X');
        assertThat(word.data()).isEqualTo("Xfoobar");
    }

    @Test
    void allotFillsWithBlancs() {
        word.data("foobar");
        word.allot(5);
        assertThat(word.data()).isEqualTo("foobar     ");
    }

    @Test
    void allotZeroChangesNothing() {
        word.data("foobar");
        word.allot(0);
        assertThat(word.data()).isEqualTo("foobar");
    }

    @Test
    void allotNegativeChangesNothing() {
        word.data("foobar");
        word.allot(-2);
        assertThat(word.data()).isEqualTo("foobar");
    }
}
