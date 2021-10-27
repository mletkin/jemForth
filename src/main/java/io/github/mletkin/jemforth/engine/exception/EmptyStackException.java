package io.github.mletkin.jemforth.engine.exception;

/**
 * Thrown when an empty stack is accessed.
 */
public class EmptyStackException extends JemForthException {

    /**
     * Create an exception.
     */
    public EmptyStackException() {
        super("Empty Stack");
    }
}
