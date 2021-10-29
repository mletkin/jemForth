package io.github.mletkin.jemforth.engine;

/**
 * Java implemented, lambda based dictionary entry.
 *
 * Must not finish execution with engine.exit()
 *
 * This class has nothing to add to the super class.<br>
 * The better solution would be a builder for words.
 */
public class InternalWord extends Word {

    /**
     * Constructor with name and command.
     *
     * @param name
     *                 name of the word
     * @param cmd
     *                 command to execute
     */
    public InternalWord(String name, Command cmd) {
        this.name = name;
        this.cfa = cmd;
    }

}
