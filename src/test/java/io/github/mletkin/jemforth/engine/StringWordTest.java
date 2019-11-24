package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.StringWord;

public class StringWordTest extends EngineTest {

    StringWord word = new StringWord("");

    @BeforeEach
    public void setup() {
        word.xt = 0;
    }

    @Test
    public void basicFetch() {
        word.data("ABCDE");
        assertThat(word.cFetch(5)).isEqualTo((int) 'D');
        assertThat(word.cFetch(1)).isEqualTo(5);
    }

    @Test
    public void fetchWithEmptyString() {
        word.data("");
        assertThat(word.cFetch(4)).isEqualTo(0);
        assertThat(word.cFetch(1)).isEqualTo(0);
    }

    @Test
    public void fetchWithNull() {
        word.data(null);
        assertThat(word.cFetch(103)).isEqualTo(0);
        assertThat(word.cFetch(0)).isEqualTo(0);
    }

    @Test
    public void basicStore() {
        word.data("ABCDE");
        word.cStore(5, (int) 'X');
        assertThat(word.data()).isEqualTo("ABCXE");
        assertThat(word.cFetch(5)).isEqualTo((int) 'X');
        assertThat(word.cFetch(1)).isEqualTo(5);
    }

    @Test
    public void storeWithEmptyString() {
        word.data("");
        word.cStore(4, (int) 'X');
        assertThat(word.data()).isEqualTo("  X");
        assertThat(word.cFetch(1)).isEqualTo(3);
    }

    @Test
    public void storeWithNull() {
        word.data(null);
        word.cStore(4, (int) 'X');
        assertThat(word.data()).isEqualTo("  X");
        assertThat(word.cFetch(1)).isEqualTo(3);
    }

    @Test
    public void storeWithStringTooShort() {
        word.data("ABC");
        word.cStore(7, (int) 'X');
        assertThat(word.data()).isEqualTo("ABC  X");
        assertThat(word.cFetch(1)).isEqualTo(6);
    }

}
