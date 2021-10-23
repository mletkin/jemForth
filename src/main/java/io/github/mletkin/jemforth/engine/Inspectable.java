/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.exception.NotSupportedException;

/**
 * Methods for Execution and Inspection from outside the engine.
 * <p>
 * The interface exposes all facilities to be used from outside the engine. This
 * might be an IDE or console application. The engine exposes the internal data
 * structure (like stacks) and does not copy them. This is not thread save and
 * might easyly be misused, but it is more efficient.
 */
public interface Inspectable {

    /**
     * Name of the Word, used for pushing Strings in colon definitions,
     */
    String STRING_LITERAL = "(STRLITERAL)";

    /**
     * Gets the data stack.
     *
     * @return the return stack instance or {@code null}
     */
    IntegerStack getDataStack();

    /**
     * Gets the return stack.
     *
     * @return the return stack instance or {@code null}
     */
    ReturnStack getReturnStack();

    /**
     * Gets the return stack content as stream.
     *
     * @return a stream of the return stack content
     */
    Stream<Integer> getReturnStackContent();

    /**
     * Gets the {@code Dictionary} instance connected to the engine.
     *
     * @return the dictionary or {@code null}.
     */
    Dictionary getDictionary();

    /**
     * Gets the {@code Inspector} instance.
     *
     * @return the inspector or {@code null}
     */
    Inspector getInspector();

    /**
     * Gets the current Interpreter pointer(IP) value.
     *
     * @return address currently stored in IP
     */
    int getIp();

    /**
     * Gets the current number base of the engine.
     *
     * @return the number base (2..72)
     */
    int getBase();

    /**
     * Executes a forth expression.
     *
     * @param expression
     *                       string containing a forth expression
     */
    default void process(String expression) {
        throw new NotSupportedException();
    }

    /**
     * Gets the debug callback function.
     *
     * The callback function is executed in the inner interpretation loop.
     *
     * @return debug callback function or {@code null}
     */
    Callback getDebugCallback();

    /**
     * Sets the debug callback function.
     *
     * @param callback
     *                     debug callback function
     */
    void setDebugCallback(Callback callback);

    /**
     * Resets the engine state.
     * <ul>
     * <li>stacks are emptied
     * <li>string buffers "word" and "tib" are emptied
     * <li>the instruction pointer is set to zero
     * <li>the engine state is set to "interpret"
     * </ul>
     *
     * @param executionOnly
     *                          reset the engine only when it is in execution of a
     *                          lines
     */
    void reset(boolean executionOnly);

    /**
     * Prints to the engine's output stream.
     *
     * @param str
     *                string to put to the output stream
     */
    void print(String str);

    /**
     * Formats a number according to the current base setting.
     *
     * @param number
     *                   number to format
     * @return the formatted number as String
     */
    String formatNumber(long number);

    /**
     * Gets the state of the engine.
     *
     * @return the engine's compile/interpret state
     */
    int getState();

    /**
     * Replace the string printer command of the engine.
     *
     * @param printer
     *                    lambda expression to use for printing
     */
    void setStringPrinter(Consumer<String> printer);

    /**
     * Replace the char printer command of the engine.
     *
     * @param printer
     *                    lambda expression to use for printing
     */
    void setCharPrinter(Consumer<Character> printer);

    /**
     * Sets the function that reads a charater from the termina.
     *
     * @param function
     *                     The function fo set
     */
    void setReadChar(Supplier<Character> function);

    /**
     * Sets the function that checks if a charater is available for reading.
     *
     * @param function
     *                     The function to set
     */
    void setIsCharAvailable(Supplier<Boolean> function);

}
