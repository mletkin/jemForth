package io.github.mletkin.jemforth.engine.exception;

/**
 * Thrown when the mass storage access fails.
 */
public class MassStorageException extends JemForthException {

    /**
     * Create an exception.
     */
    public MassStorageException() {
        super("No mass storage defined.");
    }

    /**
     * Create an exception.
     *
     * @param cause
     *                  the original exception that caused the failure
     */
    public MassStorageException(Throwable cause) {
        super(cause);
    }

}
