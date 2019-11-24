package io.github.mletkin.jemforth.engine.exception;

import io.github.mletkin.jemforth.engine.Word;

public class NotCellAlignedException extends JemForthException {

    public NotCellAlignedException(Word word) {
        super("cell access in " + word.name() + "[" + word.xt + "] not allowed");
    }
}
