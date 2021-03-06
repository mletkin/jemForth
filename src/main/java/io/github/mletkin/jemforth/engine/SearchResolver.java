/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

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
 * Forth 2012 replaced the original vocabulary system with the concept of search orders. The
 * {@code SearchResolver} tries to implement the search ordder concept and make it possible to use
 * it for the original vocabulary concept as well.
 * <ul>
 * <li>Every dictionary is identified by a numeric identifier, the <code>wid</code>.
 * <li>A <code>wid</code> can be reassigned, when a vocabulary is dropped.
 * <li>The first vocabulary defined is the default vocabulary
 * </ul>
 */
public class SearchResolver {

    // max. number of vocabularies
    private static final byte NUM_VOC = 16;

    // The wid of the default dictionary
    private int defaulthWid = 0;

    // The list of directories
    private VocabularyWord[] vocabularies = new VocabularyWord[NUM_VOC];

    // current: new words are added to this vocabulary
    // 2012 naming: compilation word list
    private Integer compilationVocabulary;

    // 2012 replaces the search vocabulary "context" with a list "searchorder"
    // search order is from high to low
    private List<Integer> searchOrder = new ArrayList<>();

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
     * Returns the compilation vocabulary.
     * <p>
     *
     * @return the compilation vocabulary.
     */
    public VocabularyWord getCurrentVocabulary() {
        return vocabularies[compilationVocabulary];
    }

    /**
     * Sets the compilation vocabulary.
     * <p>
     * see 16.6.1.2195 SET-CURRENT
     *
     * @param wid
     *            The wid of the new compilation vocabulary
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
     *            the wib of the search vocabulary
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
        try {
            if (vocabularies[wid.intValue()] == null) {
                throw new IllegalVocabularyAccess(wid);
            }
        } catch (Exception e) {
            throw new IllegalVocabularyAccess(wid);
        }
    }

    /**
     * Creates a vocabulary and adds it to the vocabulary list.
     * <p>
     * The first vocabulary that is created is the default vocabulary
     *
     * @param name
     *            name of the vocabulary
     * @return new {@link VocabularyWord} instance
     */
    public VocabularyWord createVocabulary(String name) {
        return addVocabulary(new VocabularyWord(name, freeSlot()));
    }

    private int freeSlot() {
        for (byte n = 0; n < NUM_VOC; n++) {
            if (vocabularies[n] == null) {
                return n;
            }
        }
        throw new TooManyVocabulariesException(NUM_VOC);
    }

    private VocabularyWord addVocabulary(VocabularyWord word) {
        setDefaultIfFirstVocabulary(word);
        return vocabularies[word.getWordListIdentifier()] = word;
    }

    private void setDefaultIfFirstVocabulary(VocabularyWord word) {
        if (Arrays.stream(vocabularies).allMatch(Objects::isNull)) {
            defaulthWid = word.getWordListIdentifier();
            searchOrder.add(defaulthWid);
            compilationVocabulary = defaulthWid;
        }
    }

    /**
     * Removes a vocabulary.
     *
     * @param voc
     *            the vocabulary to remove
     */
    public void forgetVocabulary(VocabularyWord voc) {
        int wid = voc.getWordListIdentifier();
        vocabularies[wid] = null;
        searchOrder.removeIf(value -> value == wid);
        if (getCurrent() == wid) {
            setCurrent(defaulthWid);
        }
    }

    /**
     * Returns the vocabulary with the given wid.
     *
     * @param wid
     *            the wid of the vocabulary to search for
     * @return the vocabulary associated with the wid
     */
    public VocabularyWord getVocabulary(int wid) {
        return vocabularies[wid];
    }

    /**
     * Searches for a word definition by name.
     *
     * @param name
     *            name of the wanted word
     * @return the {@link Word}-Object or @code null}
     */
    public Word find(String name) {
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
     *            the vocabulary to search
     * @param name
     *            the name of teh word wanted
     * @return the {@code Word} object found or {@code null}.
     */
    private Word find(VocabularyWord voc, String name) {
        return ofNullable(voc) //
                .map(v -> v.find(name)) //
                .orElse(null);
    }

}
