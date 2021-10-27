package io.github.mletkin.jemforth.engine.exception;

/**
 * Thrown when an action is illegaly taken in interpret state.
 */
public class IllegalInInterpretStateException extends JemForthException {

    /**
     * Create an exception.
     */
    public IllegalInInterpretStateException() {
        super("command not allowed in interpretation");
    }
}
