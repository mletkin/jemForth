package io.github.mletkin.jemforth.engine.exception;

public class IllegalStringLengthException extends JemForthException {

    public IllegalStringLengthException(int value) {
        super("String length " + value + " is not valid.");
    }

}
