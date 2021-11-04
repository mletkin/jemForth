/**
 * The JemForth project
 *
 * (C) 2018 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine.f83;

import java.io.IOException;
import java.util.stream.Collectors;

import io.github.mletkin.jemforth.engine.Callback;
import io.github.mletkin.jemforth.engine.JemEngine;
import io.github.mletkin.jemforth.engine.words.UserVariableWord;
import io.github.mletkin.jemforth.engine.words.Word;

/**
 * Add interactive I/O to a Forth engine.
 * <ul>
 * <li>Redefines KEY to use the I/O channel.
 * <li>Sets the engines printer to use the I/O channel.
 * <li>Controls the debug command through a user variable
 * </ul>
 */
public abstract class ConsoleEnvironment {

    protected int debug;

    protected JemEngine engine;

    public ConsoleEnvironment(JemEngine engine) {
        this.engine = engine;
        extendEngine();
    }

    /**
     * Returns the connected engine.
     *
     * @return the connected engine
     */
    public JemEngine engine() {
        return this.engine;
    }

    protected void extendEngine() {
        engine.setStringPrinter(this::print);
        engine.setCharPrinter(this::print);
        engine.setReadChar(this::readChar);
        engine.setIsCharAvailable(this::isCharAvailable);
        engine.getDictionary().add(new UserVariableWord("DEBUG", () -> debug, v -> setDebug(v)));
    }

    /**
     * Toggles debug output for console.
     *
     * @param debug
     *            a value greater zero enables debugging
     */
    protected void setDebug(int debug) {
        this.debug = debug;
        engine.setDebugCallback(debug > 0 ? this::printDebugMessage : Callback.NOP);
    }

    private void printDebugMessage(JemEngine engine) {
        System.err.printf("%10d %s RS: %s\n", engine.getIp(), wordAsString(engine, engine.getIp()),
                engine.getReturnStackContent().map(i -> i + " " + wordAsString(engine, i))
                        .collect(Collectors.joining("/")) + engine.wordBuffer.data());
    }

    private String wordAsString(JemEngine engine, int adr) {
        Word word = engine.getDictionary().fetchWord(adr);
        return (word != null ? word.name() : "") + "[" + (adr > 0 ? engine.getDictionary().fetch(adr) : "null") + "]";
    }

    /**
     * Waits for a single character on the input stream.
     * <p>
     *
     * @return the character read
     * @throws IOException
     *             key is not responsible to catch I/O-Exceptions
     */
    protected abstract char key() throws IOException;

    protected abstract void print(char value);

    protected abstract void print(String value);

    protected abstract char readChar();

    protected abstract boolean isCharAvailable();

}
