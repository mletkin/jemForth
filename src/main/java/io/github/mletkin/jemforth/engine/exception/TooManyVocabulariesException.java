package io.github.mletkin.jemforth.engine.exception;

public class TooManyVocabulariesException extends JemForthException {

    public TooManyVocabulariesException(int number) {
        super("Only " + number + " vocabularies are allowed.");
    }
}
