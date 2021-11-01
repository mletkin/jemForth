package io.github.mletkin.jemforth.engine.exception;

/**
 * Thrown when the string length is set to an out of range value.
 */
public class IllegalStringLengthException extends JemForthException {

    /**
     * Create an exception.
     *
     * @param value
     *                  the length that is illegal
     */
    public IllegalStringLengthException(int value) {
        super("String length " + value + " is not valid.");
    }

}
