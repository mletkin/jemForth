package io.github.mletkin.jemforth.engine.exception;

public class IllegalMemoryAccessException extends JemForthException {

    public IllegalMemoryAccessException() {
        super("memory access violation");
    }
}
