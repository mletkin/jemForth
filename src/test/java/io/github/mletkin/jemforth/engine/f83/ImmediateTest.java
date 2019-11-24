package io.github.mletkin.jemforth.engine.f83;

import static io.github.mletkin.jemforth.engine.harness.Fixture.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.jemforth.engine.Word;
import io.github.mletkin.jemforth.engine.f83.Forth83Engine;

/**
 * check that immediate words are defined immediate
 */
public class ImmediateTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("testData")
    public void test(String cmd) {
        Word word = fixture(new Forth83Engine()).engine().getDictionary().find(cmd);
        assertThat(word).as("%s is not defined", cmd).isNotNull();
        assertThat(word.isImmediate()).as("%s is not immediate", cmd).isTrue();
    }

    public static Stream<String> testData() {
        return Stream.of(//
                "(", //
                "+LOOP", //
                ".\"", //
                ".(", //
                ";", //
                "ABORT\"", //
                "BEGIN", //
                "DO", //
                "DOES>", //
                "ELSE", //
                "IF", //
                "LEAVE", //
                "LITERAL", //
                "LOOP", //
                "REPEAT", //
                "THEN", //
                "WHILE", //
                "[", //
                "[']", //
                "[COMPILE]", //
                "END", //
                "RECURSE", //
                "UNTIL", //
                "DLITERAL", //
                "[CHAR]"//
        );
    }

}
