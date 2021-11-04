package io.github.mletkin.jemforth.engine.words;

import io.github.mletkin.jemforth.engine.Command;

/**
 * Java implemented, lambda based dictionary entry.
 *
 * Must not finish execution with engine.exit()
 *
 * This class has nothing to add to the super class.<br>
 * The better solution might be a builder for words.
 */
public class InternalWord extends Word {

    /**
     * Creates a new internal word.
     *
     * @param name
     *                 name of the word
     * @param cmd
     *                 command to execute
     */
    public InternalWord(String name, Command cfa) {
        super(name);
        this.cfa = cfa;
    }

}
