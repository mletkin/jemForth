package io.github.mletkin.jemforth.engine;

/**
 * Definition of the debug callback function for the JemEngine.
 */
@FunctionalInterface
public interface Callback {

    /**
     * callback function that does nothing.
     */
    static final Callback NOP = e -> {};

    /**
     * Executes the call back function.
     *
     * @param engine
     *            context of the function, engine for which it is called.
     */
    void call(JemEngine engine);
}
