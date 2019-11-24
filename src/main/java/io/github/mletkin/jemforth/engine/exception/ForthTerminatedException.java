package io.github.mletkin.jemforth.engine.exception;

public class ForthTerminatedException extends JemForthException {

    public ForthTerminatedException() {
        super("terminated");
    }
}
