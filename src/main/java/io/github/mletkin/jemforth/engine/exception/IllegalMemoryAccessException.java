package io.github.mletkin.jemforth.engine.exception;

/**
 * Thrown when the memory is accessed in a forbidden way.
 * <p>
 * i.e. access to a non existing word
 */
public class IllegalMemoryAccessException extends JemForthException {

    /**
     * Create an exception.
     */
    public IllegalMemoryAccessException() {
        super("memory access violation");
    }
}
