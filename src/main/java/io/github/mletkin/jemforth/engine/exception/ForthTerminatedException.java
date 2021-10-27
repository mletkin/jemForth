package io.github.mletkin.jemforth.engine.exception;

/**
 * Indicates the termination of the forth engine.
 */
public class ForthTerminatedException extends JemForthException {

    /**
     * create an exception.
     */
    public ForthTerminatedException() {
        super("terminated");
    }

}
