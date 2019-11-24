package io.github.mletkin.jemforth.engine.exception;

public class IllegalVocabularyAccess extends JemForthException {
    public IllegalVocabularyAccess(Integer wid) {
        super("cannot access vocabulary " + wid);
    }
}
