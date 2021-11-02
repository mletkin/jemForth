package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import org.junit.jupiter.api.Test;

class VocabularyWordTest {

    private JemEngine engine = TestUtils.mkEngine();
    private VocabularyWord voc = (VocabularyWord) engine.getDictionary().add(new VocabularyWord("myVoc"));

    @Test
    void executeSetsVocabularyAsOrder() {
        voc.execute(engine);

        assertThat(engine.dictionary.getSearchResolver().getOrder()).contains(voc.getWordListIdentifier());
    }

    @Test
    void addingAwordMakesItFindable() {
        Word word = mkNamedWord("foobar");
        voc.add(word);
        assertThat(voc.find("foobar")).isSameAs(word);
    }

    @Test
    void forgettingAwordMakesItUnFindable() {
        Word word = mkNamedWord("foobar");
        voc.add(word);
        assumeThat(voc.find("foobar")).isSameAs(word);
        voc.forget(word);
        assertThat(voc.find("foobar")).isNull();
    }

    private Word mkNamedWord(String theName) {
        Word word = new Word() {
            {
                this.name = theName;
            }
        };
        return word;
    }

    @Test
    void toStringContainsWid() {
        assertThat(voc.toString()).isEqualTo("myVoc[" + voc.xt() + ":" + voc.getWordListIdentifier() + "]");
    }
}
