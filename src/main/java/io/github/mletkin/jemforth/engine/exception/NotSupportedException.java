package io.github.mletkin.jemforth.engine.exception;

public class NotSupportedException extends JemForthException {

    public NotSupportedException() {
        super("function not supported by the machine.");
    }
}
