package io.github.mletkin.jemforth.engine.exception;

/**
 * Thrown when no more vocabulary slots are available.
 */
public class TooManyVocabulariesException extends JemForthException {

    /**
     * Creates an exception.
     *
     * @param number
     *                   number of available vocabularies
     */
    public TooManyVocabulariesException(int number) {
        super("Only " + number + " vocabularies are allowed.");
    }

}
