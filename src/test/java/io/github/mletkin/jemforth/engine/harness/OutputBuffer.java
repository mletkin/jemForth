/**
 * The JemForth project
 *
 * (C) by the Big Shedder 2018
 */
package io.github.mletkin.jemforth.engine.harness;

import io.github.mletkin.jemforth.engine.JemEngine;

/**
 * Buffer to collect the output of a forth engine.
 * <p>
 * Provides and attaches the methods for output redirection to the forth
 * engine.<br>
 * To be used by the {@code Fixture} class only hence package visible.
 */
class OutputBuffer {

    private String result = "";

    /**
     * Constructs a result buffer and attachs it to the engine.
     * <p>
     *
     * @param engine
     *            the engine to be embedded in the buffer
     */
    OutputBuffer(JemEngine engine) {
        attachEngine(engine);
    }

    /**
     * Resets the buffer content.
     */
    void reset() {
        result = "";
    }

    /**
     * The current content of the buffer.
     *
     * @return the current content of the buffer
     */
    String content() {
        return result;
    }

    /**
     * Adds a string to the buffer.
     * <p>
     * Used as output hook for the embedded engine
     *
     * @param s
     *            string to add
     */
    private void add(String s) {
        result += (s != null ? s : "");
    }

    /**
     * Adds a character to the buffer.
     * <p>
     * Used as output hook for the embedded engine
     *
     * @param c
     *            character to add
     */
    private void add(char c) {
        result += c;
    }

    /**
     * Attaches the test engine to the result buffer.
     *
     * @param engine
     *            the engine to attach
     */
    private void attachEngine(JemEngine engine) {
        engine.setStringPrinter(this::add);
        engine.setCharPrinter(this::add);
    }
}
