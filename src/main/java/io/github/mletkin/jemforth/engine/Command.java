package io.github.mletkin.jemforth.engine;

/**
 * Representation of a Java executable cfa command.
 */
@FunctionalInterface
public interface Command<T extends JemEngine> {

    Command<JemEngine> NOP = (JemEngine c) -> {};

    /**
     * Execute the command.
     *
     * Every word is bound to a single forth engine.<br>
     * Additionally it is passed as a parameter.
     *
     * @param context
     *                    The engine context for the command
     */
    void execute(T context);
}
