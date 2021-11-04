/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine.words;

import static io.github.mletkin.jemforth.engine.Util.reverse;

import java.util.ArrayList;
import java.util.List;

/**
 * Word that defines a Vocabulary.
 *
 * Execution sets the context.
 */
public class VocabularyWord extends Word {

    /**
     * The word list identifier of the vocabulary.
     */
    private Integer wid = null;

    /**
     * The words contained in this vocabulary in the order of definition.
     */
    private final List<Word> memory = new ArrayList<>();

    /**
     * Creates a new vocabulary word.
     *
     * @param name
     *                 the name of the word
     */
    public VocabularyWord(String name) {
        super(name);
    }

    /**
     * Sets the vocabulary id.
     *
     * @param wid
     *                the vocabulary id
     */
    void setWid(Integer wid) {
        this.wid = wid;
        cfa = e -> e.getDictionary().getSearchResolver().setOrder(wid);
    }

    /**
     * Returns the word list identifier of this vocabulary.
     *
     * @return the identifier of this vocabulary
     */
    public Integer getWordListIdentifier() {
        return wid;
    }

    /**
     * Adds a word to this vocabulary.
     *
     * @param word
     *                 word to be added
     */
    void add(Word word) {
        word.vocabulary = wid;
        memory.add(word);
    }

    /**
     * Removes a word from this vocabulary.
     *
     * @param word
     *                 the word to remove
     */
    void forget(Word word) {
        memory.remove(word);
    }

    /**
     * Retrieves a word in this vocabulary.
     *
     * @param name
     *                 name of the word wanted
     * @return the Word found or {@code null}
     */
    Word find(String name) {
        return reverse(memory) //
                .filter(word -> name.equals(word.name())) //
                .findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return name() + "[" + xt + ":" + wid + "]";
    }
}