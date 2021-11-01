package io.github.mletkin.jemforth.engine.exception;

/**
 * Thrown when a non existing vocabulary is accessed.
 */
public class IllegalVocabularyAccess extends JemForthException {

    /**
     * Create an exception.
     *
     * @param wid
     *                id of the vocabulary looked for
     */
    public IllegalVocabularyAccess(Integer wid) {
        super("cannot access vocabulary " + wid);
    }
}
