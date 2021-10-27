package io.github.mletkin.jemforth.engine.exception;

/**
 * Thrown when a non existing mass storage block is accessed.
 */
public class MassStorageCapacityExceededException extends JemForthException {

    /**
     * Create an exception.
     */
    public MassStorageCapacityExceededException() {
        super("Mass storage capacity exceeded");
    }

}
