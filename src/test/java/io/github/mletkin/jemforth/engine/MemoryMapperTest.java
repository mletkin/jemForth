package io.github.mletkin.jemforth.engine;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MemoryMapperTest {

    @ParameterizedTest
    @MethodSource
    void lastTwoBitsAreNullified(int input, int result) {
        Assertions.assertThat(MemoryMapper.toCellLocator(input)).isEqualTo(result);
    }

    static Stream<Arguments> lastTwoBitsAreNullified() {
        return Stream.of( //
                Arguments.of(0x7FF0, 0x7FF0), //
                Arguments.of(0x7FF1, 0x7FF0), //
                Arguments.of(0x7FF1, 0x7FF0), //
                Arguments.of(0x7FF3, 0x7FF0), //
                Arguments.of(0x7FF4, 0x7FF4), //
                Arguments.of(0x7FF5, 0x7FF4), //
                Arguments.of(0x7FF6, 0x7FF4), //
                Arguments.of(0x7FF7, 0x7FF4), //
                Arguments.of(0x7FF8, 0x7FF8), //
                Arguments.of(0x7FF9, 0x7FF8), //
                Arguments.of(0x7FFA, 0x7FF8), //
                Arguments.of(0x7FFB, 0x7FF8), //
                Arguments.of(0x7FFC, 0x7FFC), //
                Arguments.of(0x7FFD, 0x7FFC), //
                Arguments.of(0x7FFE, 0x7FFC), //
                Arguments.of(0x7FFF, 0x7FFC), //
                Arguments.of(0x0000, 0x0000), //
                Arguments.of(0x0001, 0x0000), //
                Arguments.of(0x0001, 0x0000), //
                Arguments.of(0x0003, 0x0000), //
                Arguments.of(0x0004, 0x0004), //
                Arguments.of(0x0005, 0x0004), //
                Arguments.of(0x0006, 0x0004), //
                Arguments.of(0x0007, 0x0004), //
                Arguments.of(0x0008, 0x0008), //
                Arguments.of(0x0009, 0x0008), //
                Arguments.of(0x000A, 0x0008), //
                Arguments.of(0x000B, 0x0008), //
                Arguments.of(0x000C, 0x000C), //
                Arguments.of(0x000D, 0x000C), //
                Arguments.of(0x000E, 0x000C), //
                Arguments.of(0x000F, 0x000C) //

        );
    }

}
