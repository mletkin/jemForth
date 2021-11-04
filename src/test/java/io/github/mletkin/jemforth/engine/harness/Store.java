package io.github.mletkin.jemforth.engine.harness;

/**
 * Container for a single value of a given Type.<üp> Primarily used for the
 * testing of lambda injection.
 *
 * @param <E>
 *                the type of the stored value
 */
public class Store<E> {
    private E value;

    /**
     * Sets the value.
     *
     * @param value
     *                  the value to store
     */
    public void set(E value) {
        this.value = value;
    }

    /**
     * Gets the stored value.
     *
     * @return the stored value
     */
    public E get() {
        return value;
    }

}
