/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine.words;

import static io.github.mletkin.jemforth.engine.Util.reverse;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.exception.IllegalVocabularyAccess;
import io.github.mletkin.jemforth.engine.exception.TooManyVocabulariesException;

/**
 * Keeper of vocabularies and Search orders.
 * <p>
 * Forth 2012 replaced the original vocabulary system with the concept of search
 * orders. The {@code SearchResolver} tries to implement the search ordder
 * concept and make it possible to use it for the original vocabulary concept as
 * well.
 * <ul>
 * <li>Every dictionary is identified by a numeric identifier, the
 * <code>wid</code>.
 * <li>A <code>wid</code> can be reassigned, when a vocabulary is dropped.
 * <li>The first vocabulary defined is the default vocabulary
 * </ul>
 */
public class SearchResolver {

    /**
     * Maximum number of vocabularies.
     */
    private static final byte NUM_VOC = 16;

    /**
     * The wid of the default dictionary.
     */
    private int defaulthWid = 0;

    /**
     * The list of directories.
     */
    private final VocabularyWord[] vocabularies = new VocabularyWord[NUM_VOC];

    /**
     * Order in which the directories are searched.<br>
     * 2012 replaces the search vocabulary "context" with a list "searchorder"
     * search order is from high to low
     */
    private final List<Integer> searchOrder = new ArrayList<>();

    /**
     * current: new words are added to this vocabulary.<br>
     * 2012 naming: compilation word list
     */
    private Integer compilationVocabulary;

    /**
     * Returns the wid of the compilation vocabulary.
     * <p>
     * see16.6.1.1643 GET-CURRENT
     *
     * @return the wib of the current compilation vocabulary
     */
    public Integer getCurrent() {
        return compilationVocabulary;
    }

    /**
     * Sets the compilation vocabulary.
     * <p>
     * see 16.6.1.2195 SET-CURRENT
     *
     * @param wid
     *                The wid of the new compilation vocabulary
     */
    public void setCurrent(Integer wid) {
        isValidWid(wid);
        compilationVocabulary = wid;
    }

    /**
     * Returns the search list of vocabularies.
     * <p>
     * see 16.6.1.1647 GET-ORDER
     *
     * @return The wids of the current search list vocabularies
     */
    public Stream<Integer> getOrder() {
        return reverse(searchOrder);
    }

    /**
     * Returns the first search vocabulary.
     * <p>
     * For F83 implementations that support only one search vocabulary
     *
     * @return the wid of the first search vocabulary of the list
     * @deprecated use getOrder instead
     */
    @Deprecated
    public Integer getContext() {
        return getOrder().findFirst().orElseThrow(null);
    }

    /**
     * Sets the search order to the given vocabulary.
     * <p>
     * For F83 implementations that support only one search vocabulary.<br>
     * The default vocabulary is always the last vocabulary in the list.
     *
     * @param context
     *                    the wib of the search vocabulary
     */
    public void setOrder(Integer context) {
        isValidWid(context);
        searchOrder.clear();
        if (context != defaulthWid) {
            searchOrder.add(defaulthWid);
        }
        searchOrder.add(context);
    }

    private void isValidWid(Integer wid) {
        if (wid < 0 || wid >= NUM_VOC || vocabularies[wid.intValue()] == null) {
            throw new IllegalVocabularyAccess(wid);
        }
    }

    /**
     * Adds a word to the current vocabulary.
     * <p>
     * Vocabulary words are added to the vocabulary list as well.
     *
     * @param word
     *                 word to add
     */
    void add(Word word) {
        if (word instanceof VocabularyWord) {
            addVocabulary((VocabularyWord) word);
        }
        vocabularies[compilationVocabulary].add(word);
    }

    /**
     * Adds a new vocabulary word.
     * <p>
     * The first vocabulary that is created is the default vocabulary
     *
     * @param word
     *                 the vocabulary to add
     */
    private void addVocabulary(VocabularyWord word) {
        word.setWid(freeSlot());
        setDefaultIfFirstVocabulary(word);
        vocabularies[word.getWordListIdentifier()] = word;
    }

    private int freeSlot() {
        for (byte n = 0; n < NUM_VOC; n++) {
            if (vocabularies[n] == null) {
                return n;
            }
        }
        throw new TooManyVocabulariesException(NUM_VOC);
    }

    private void setDefaultIfFirstVocabulary(VocabularyWord word) {
        if (Arrays.stream(vocabularies).allMatch(Objects::isNull)) {
            defaulthWid = word.getWordListIdentifier();
            searchOrder.add(defaulthWid);
            compilationVocabulary = defaulthWid;
        }
    }

    /**
     * Removes a word.
     *
     * @param word
     *                 word to remove
     */
    void forgetWord(Word word) {
        vocabularies[word.vocabulary].forget(word);
        if (word instanceof VocabularyWord) {
            forgetVocabulary((VocabularyWord) word);
        }
    }

    /**
     * Removes a vocabulary.
     *
     * @param voc
     *                the vocabulary to remove
     */
    private void forgetVocabulary(VocabularyWord voc) {
        int wid = voc.getWordListIdentifier();
        vocabularies[wid] = null;
        searchOrder.removeIf(value -> value == wid);
        if (getCurrent() == wid) {
            setCurrent(defaulthWid);
        }
    }

    /**
     * Searches for a word definition by name.
     *
     * @param name
     *                 name of the wanted word
     * @return the {@link Word}-Object or @code null}
     */
    Word find(String name) {
        return getOrder() //
                .map(wib -> vocabularies[wib]) //
                .map(v -> this.find(v, name)) //
                .filter(Objects::nonNull) //
                .findFirst().orElse(null);
    }

    /**
     * Search a word in a specific vocabulary.
     *
     * @param voc
     *                 the vocabulary to search
     * @param name
     *                 the name of teh word wanted
     * @return the {@code Word} object found or {@code null}.
     */
    private Word find(VocabularyWord voc, String name) {
        return ofNullable(voc) //
                .map(v -> v.find(name)) //
                .orElse(null);
    }

}
