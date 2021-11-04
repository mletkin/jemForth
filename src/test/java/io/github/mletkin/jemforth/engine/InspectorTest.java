package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.words.InternalWord;
import io.github.mletkin.jemforth.engine.words.Word;

public class InspectorTest {

    Inspector inspector = new Inspector(null, null);

    @Test
    public void seeNullReturnsMessage() {
        assertThat(inspector.see(null)).isEqualTo("\nword not found ");
    }

    @Test
    public void seeWordWithoutComment() {
        Word word = new InternalWord("name", c -> {});
        assertThat(inspector.see(word)).isEqualTo("\ninternal name ");
    }

    @Test
    public void seeWordWithComment() {
        Word word = new InternalWord("name", c -> {}).comment("Kommentar");
        assertThat(inspector.see(word)).isEqualTo("\nKommentar\ninternal name ");
    }

}
