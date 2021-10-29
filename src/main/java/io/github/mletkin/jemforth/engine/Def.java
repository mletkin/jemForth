package io.github.mletkin.jemforth.engine;

/**
 * Convenience definition class for a word used in an Engine.
 */
public class Def<T extends JemEngine> {

    private Command<T> cmd;
    private String[] comment;

    /**
     * Create a new Definition.
     *
     * @param <U>
     *                    Type of the engine
     * @param cmd
     *                    command to execute
     * @param comment
     *                    array of comment strings, might be empty
     * @return the new definition
     */
    public static <U extends JemEngine> Def<U> of(Command<U> cmd, String... comment) {
        Def<U> def = new Def<U>();
        def.cmd = cmd;
        def.comment = comment;
        return def;
    }

    /**
     * Returns the command.
     *
     * @return the definition
     */
    public Command<T> cmd() {
        return cmd;
    }

    /**
     * Returns the comments
     *
     * @return the comment array
     */
    public String[] comment() {
        return comment;
    }

}
