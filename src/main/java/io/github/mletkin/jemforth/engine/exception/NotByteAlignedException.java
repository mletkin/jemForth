package io.github.mletkin.jemforth.engine.exception;

import io.github.mletkin.jemforth.engine.words.Word;

/**
 * Thrown when a byte is accessed in a word that's not byte aligned.
 */
public class NotByteAlignedException extends JemForthException {

    /**
     * Create an eception.
     *
     * @param word
     *                 the word that is accesed
     */
    public NotByteAlignedException(Word word) {
        super("byte access in " + word.name() + "[" + word.xt() + "] not allowed");
    }

}
