package io.github.mletkin.jemforth.engine.exception;

import java.io.IOException;

public class MassStorageNotAvailableException extends JemForthException {

    public MassStorageNotAvailableException(IOException cause) {
        super(cause);
    }

}
