package io.github.mletkin.jemforth.engine.exception;

/**
 * Thrownby the default implementation of {@code Inspectable.process()}.
 */
public class NotSupportedException extends JemForthException {

    /**
     * Creates an exception.
     */
    public NotSupportedException() {
        super("function not supported by the machine.");
    }

}
