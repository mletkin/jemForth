package io.github.mletkin.jemforth.engine.exception;

public class EmptyStackException extends JemForthException {

    public EmptyStackException() {
        super("Empty Stack");
    }
}
