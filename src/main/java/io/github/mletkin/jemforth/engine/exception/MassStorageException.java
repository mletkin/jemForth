package io.github.mletkin.jemforth.engine.exception;

public class MassStorageException extends JemForthException {
    public MassStorageException(Throwable cause) {
        super(cause);
    }

    public MassStorageException() {
        super("No mass storage defined.");
    }

}
