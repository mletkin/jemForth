package io.github.mletkin.jemforth.engine.exception;

import io.github.mletkin.jemforth.engine.Word;

public class NotByteAlignedException extends JemForthException {

    public NotByteAlignedException(Word word) {
        super("byte access in " + word.name() + "[" + word.xt + "] not allowed");
    }
}
