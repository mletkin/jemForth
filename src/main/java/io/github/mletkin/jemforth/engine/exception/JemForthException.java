package io.github.mletkin.jemforth.engine.exception;

/**
 * Generic Exception thrown in a forth engine.
 */
public class JemForthException extends RuntimeException {

    public JemForthException() {
        super();
    }

    public JemForthException(String message) {
        super(message);
    }

    public JemForthException(Throwable cause) {
        super(cause);
    }

    public JemForthException(String message, Throwable cause) {
        super(message, cause);
    }

    public JemForthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
