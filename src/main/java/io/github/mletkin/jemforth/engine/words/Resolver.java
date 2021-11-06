package io.github.mletkin.jemforth.engine.words;

public interface Resolver {

    /**
     * Returns the wid of the compilation vocabulary.
     * <p>
     * see 16.6.1.1643 GET-CURRENT
     *
     * @return the wib of the current compilation vocabulary
     */
    Integer getCurrent();

    /**
     * Sets the compilation vocabulary.
     * <p>
     * see 16.6.1.2195 SET-CURRENT
     *
     * @param current
     *                    The wid of the new compilation vocabulary
     */
    void setCurrent(Integer current);

    /**
     * Returns the first search vocabulary.
     *
     * @return the address of the search vocabulary
     */
    Integer getContext();

    /**
     * Sets the context vocabulary.
     * <p>
     * see 16.6.1.2195 SET-CURRENT
     *
     * @param context
     *                    The address of the new context vocabulary
     */
    void setContext(Integer context);

    /**
     * Adds a word to the current vocabulary.
     *
     * Vocabulary words are added to the vocabulary list as well.
     *
     * @param word
     *                 word to add
     */
    void add(Word word);

    /**
     * Searches for a word definition by name.
     *
     * @param name
     *                 name of the wanted word
     * @return the {@link Word}-Object or @code null}
     */
    Word find(String name);

    /**
     * Removes a word.
     *
     * @param word
     *                 word to remove
     */
    void forgetWord(Word word);

}
