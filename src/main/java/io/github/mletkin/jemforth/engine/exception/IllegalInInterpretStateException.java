package io.github.mletkin.jemforth.engine.exception;

public class IllegalInInterpretStateException extends JemForthException {

    public IllegalInInterpretStateException() {
        super("command not allowed in interpretation");

    }
}
