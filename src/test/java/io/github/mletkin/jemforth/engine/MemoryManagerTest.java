package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.MemoryMapper;

public class MemoryManagerTest {

    @ParameterizedTest
    @MethodSource()
    void extractByte(int from, int position, int value, String text) {
        assertThat(MemoryMapper.extractByte(from, position)).as(text + " byte").isEqualTo(value);
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
        assertThat(MemoryMapper.setByte(within, position, value)).as(text + " byte").isEqualTo(result);
    }

    static Stream<Arguments> setByte() {
        return Stream.of( //
                Arguments.of(0x43B2C1D0, 0, 0x77, 0x43B2C177, "1st"), //
                Arguments.of(0x43B2C1D0, 1, 0x77, 0x43B277D0, "2nd"), //
                Arguments.of(0x43B2C1D0, 2, 0x77, 0x4377C1D0, "3rd"), //
                Arguments.of(0x43B2C1D0, 3, 0x77, 0x77B2C1D0, "4th") //
        );
    }

}
