package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UtilTest {

    @Test
    public void nullIsEmpty() {
        assertThat(Util.isEmpty(null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "   ", "\t\t", "\n", "\r", " \t \n\r " })
    public void whitespaceIsEmpty(String str) {
        assertThat(Util.isEmpty(str)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "xy", " x", "y   " })
    public void nonSpaceIsNotEmpty(String str) {
        assertThat(Util.isEmpty(str)).isFalse();
    }

    @Test
    public void reverseListWorks() {
        List<Integer> liste = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertThat(Util.reverse(liste)).containsExactly(9, 8, 7, 6, 5, 4, 3, 2, 1);
    }

    @Test
    public void reverseEmptyListIsEmpty() {
        assertThat(Util.reverse(Collections.emptyList())).isEmpty();
    }

    @Test
    public void testRemove() {
        List<Integer> liste = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9).stream().collect(Collectors.toList());
        Util.reverse(liste) //
                .filter(key -> key % 2 == 0) //
                .forEach(liste::remove);
        assertThat(liste).containsExactly(1, 3, 5, 7, 9);
    }

    @Test
    void nullIsSilentlyNotClosed() {
        assertThatNoException().isThrownBy(() -> Util.closeSilently(null));
    }
}
