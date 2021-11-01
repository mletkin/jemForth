package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MemoryMapperTest {

    MemoryMapper mm = new MemoryMapper();

    @ParameterizedTest
    @MethodSource()
    void extractByte(int from, int position, int value, String text) {
        assertThat(mm.extractByte(from, position)).as(text + " byte").isEqualTo(value);
    }

    static Stream<Arguments> extractByte() {
        return Stream.of( //
                Arguments.of(0xA3B2C1D0, 0, 0xD0, "1st"), //
                Arguments.of(0xA3B2C1D0, 1, 0xC1, "2nd"), //
                Arguments.of(0xA3B2C1D0, 2, 0xB2, "3rd"), //
                Arguments.of(0xA3B2C1D0, 3, 0xA3, "4th") //
        );
    }

    @ParameterizedTest
    @MethodSource()
    void setByte(int within, int position, int value, int result, String text) {
        assertThat(mm.setByte(within, position, value)).as(text + " byte").isEqualTo(result);
    }

    static Stream<Arguments> setByte() {
        return Stream.of( //
                Arguments.of(0x43B2C1D0, 0, 0x77, 0x43B2C177, "1st"), //
                Arguments.of(0x43B2C1D0, 1, 0x77, 0x43B277D0, "2nd"), //
                Arguments.of(0x43B2C1D0, 2, 0x77, 0x4377C1D0, "3rd"), //
                Arguments.of(0x43B2C1D0, 3, 0x77, 0x77B2C1D0, "4th") //
        );
    }

    @ParameterizedTest
    @MethodSource
    void lastTwoBitsAreNullified(int input, int result) {
        assertThat(mm.toCellLocator(input)).isEqualTo(result);
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
