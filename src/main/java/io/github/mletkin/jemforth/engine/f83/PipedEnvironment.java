/**
 * The JemForth project
 *
 * (C) 2018 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine.f83;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.mletkin.jemforth.engine.JemEngine;

/**
 * Glue class between a {@code JConsole} and a {@code JemForth} engine using
 * piped I/O streams.
 */
public class PipedEnvironment extends ConsoleEnvironment {

    private OutputStream output;
    private InputStream input;

    /**
     * Connects a JemForth engine with Environment.
     *
     * @param engine
     *                   the Forth engine to connectx
     */
    public PipedEnvironment(JemEngine engine) {
        super(engine);
    }

    public void setInput(InputStream input) {
        this.input = input;
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    @Override
    protected synchronized char key() throws IOException {
        while (input.available() == 0) {
            // just wait for a key
        }
        byte b[] = new byte[1];
        if (input.read(b) == -1) {
            throw new IllegalStateException();
        }
        return (char) b[0];
    }

    @Override
    protected boolean isCharAvailable() {
        try {
            return input.available() != 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected char readChar() {
        try {
            return key();
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            return (char) -1;
        }
    }

    @Override
    public void print(char character) {
        try {
            output().write(character);
            output().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void print(String str) {
        try {
            output().write(str.getBytes());
            output().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * We had some trouble with an unexpected "null" steam.
     *
     * @return the current output stream or standard Error.
     */
    private OutputStream output() {
        return output != null ? output : System.err;
    }

}
