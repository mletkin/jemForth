package io.github.mletkin.jemforth.engine.exception;

import io.github.mletkin.jemforth.engine.Word;

/**
 * Thrown when a cell is accessed in a word that's not cell aligned.
 */
public class NotCellAlignedException extends JemForthException {

    /**
     * Create an eception.
     *
     * @param word
     *                 the word that is accesed
     */
    public NotCellAlignedException(Word word) {
        super("cell access in " + word.name() + "[" + word.xt() + "] not allowed");
    }
}
