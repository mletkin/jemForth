/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine.words;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.github.mletkin.jemforth.engine.exception.IllegalVocabularyAccess;

/**
 * Keeper of vocabularies.
 * <p>
 * <ul>
 * <li>Keeps a list of all vocabularies
 * <li>The first vocabulary added is the default vocabulary
 * </ul>
 */
public class F83VocabularyAccess implements Resolver {

    /**
     * The list of directories.
     */
    private final List<VocabularyWord> vocabularies = new ArrayList<>();

    /**
     * The address of the vocabulary to which new words are added.
     */
    private VocabularyWord current;

    /**
     * The address of the vocabulary where words are looked up.
     */
    private VocabularyWord context;

    /**
     * The first voc. defines is the default, usually "FORTH".
     */
    private VocabularyWord defaultVocabulary;

    /**
     * Find a Word by its XT;
     */
    private Function<Integer, Word> byXt;

    /**
     * Creates a new resolver.
     *
     * @param byXt
     *                 function to retrieve the word
     */
    public F83VocabularyAccess(Function<Integer, Word> byXt) {
        this.byXt = byXt;
    }

    /**
     * Returns the wid of the compilation vocabulary.
     * <p>
     * see 16.6.1.1643 GET-CURRENT
     *
     * @return the wib of the current compilation vocabulary
     */
    @Override
    public Integer getCurrent() {
        return current.xt();
    }

    /**
     * Sets the compilation vocabulary.
     * <p>
     * see 16.6.1.2195 SET-CURRENT
     *
     * @param current
     *                    The wid of the new compilation vocabulary
     */
    @Override
    public void setCurrent(Integer current) {
        this.current = findVocabulary(current).orElseThrow(() -> new IllegalVocabularyAccess(current));
    }

    /**
     * Returns the first search vocabulary.
     *
     * @return the address of the search vocabulary
     */
    @Override
    public Integer getContext() {
        return context.xt();
    }

    /**
     * Sets the context vocabulary.
     * <p>
     * see 16.6.1.2195 SET-CURRENT
     *
     * @param context
     *                    The address of the new context vocabulary
     */
    @Override
    public void setContext(Integer context) {
        this.context = findVocabulary(context).orElseThrow(() -> new IllegalVocabularyAccess(context));
    }

    /**
     * Adds a word to the current vocabulary.
     *
     * Vocabulary words are added to the vocabulary list as well.
     *
     * @param word
     *                 word to add
     */
    @Override
    public void add(Word word) {
        if (word instanceof VocabularyWord vocabularyWord) {
            addVocabulary(vocabularyWord);
        }
        current.add(word);
    }

    /**
     * Searches for a word definition by name.
     *
     * @param name
     *                 name of the wanted word
     * @return the {@link Word}-Object or @code null}
     */
    @Override
    public Word find(String name) {
        Word word = context.find(name);
        if (word == null && context != defaultVocabulary) {
            word = defaultVocabulary.find(name);
        }
        return word;
    }

    /**
     * Returns the vocabulary word with the gven xt.
     *
     * @param xt
     *               xt of the vocabulary to retrieve
     * @return the vocabulary word, wrapped in an Optional
     */
    private Optional<VocabularyWord> findVocabulary(Integer xt) {
        return ofNullable(byXt.apply(xt)) //
                .filter(VocabularyWord.class::isInstance) //
                .map(VocabularyWord.class::cast);
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
        setDefaultIfFirstVocabulary(word);
        vocabularies.add(word);
    }

    private void setDefaultIfFirstVocabulary(VocabularyWord word) {
        if (vocabularies.isEmpty()) {
            defaultVocabulary = word;
            current = word;
            context = word;
        }
    }

    /**
     * Removes a word.
     *
     * @param word
     *                 word to remove
     */
    @Override
    public void forgetWord(Word word) {
        findVocabulary(word.vocabulary).ifPresent(v -> v.forget(word));
        if (word instanceof VocabularyWord vocabularyWord) {
            forgetVocabulary(vocabularyWord);
        }
    }

    /**
     * Removes a vocabulary.
     *
     * @param voc
     *                the vocabulary to remove
     */
    private void forgetVocabulary(VocabularyWord voc) {
        vocabularies.remove(voc);
        if (current == voc) {
            current = defaultVocabulary;
        }
        if (context == voc) {
            context = defaultVocabulary;
        }
    }

}
