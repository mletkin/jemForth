/**
 * The JemForth project
 *
 * (C) by the Big Shedder 2018
 */
package io.github.mletkin.jemforth.engine.harness;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;

import io.github.mletkin.jemforth.engine.JemEngine;

/**
 * Represents a line of Forth commands with conditions.
 * <p>
 * May be a single line test case or a single line of a multi line test case. Attached whith each line is the expected
 * stack. The stack content will always be checked. Conditions for the expected output and arbitrary conditions to check
 * the engine state are optional. All conditions are checked after execution of the forth commans.
 * <p>
 * Lines are created by method chaining in fluent style.<br>
 * Properties might be added but not changed after definition.
 */
public class Line implements Ignorable<Line> {

    private String cmd;
    private Integer[] stack = {};
    private List<StackExpression> xStack = new ArrayList<>();
    private List<Consumer<JemEngine>> conditions = new ArrayList<>();
    private List<Consumer<String>> outputConditions = new ArrayList<>();
    private boolean ignore;

    /**
     * Always use a factory method to instantiate new objects.
     *
     * @param cmd
     *            the command to execute
     */
    protected Line(String cmd) {
        this.cmd = cmd;
    }

    /**
     * construct a line for the given command.
     * <p>
     *
     * @param cmd
     *            the command to execute
     * @return the newly constructed line
     */
    public static Line line(String cmd) {
        return new Line(cmd);
    }

    /**
     * Sets a list of integer values as expected stack content.
     * <p>
     *
     * @param stack
     *            the values to use as stack content
     * @return the line under construction
     */
    public Line stack(Integer... stack) {
        this.stack = stack;
        return this;
    }

    /**
     * Sets an expression list as expected stack content.
     * <p>
     *
     * @param first
     *            at least one expression is needed
     * @param xStack
     *            a list of expression, that might be empty
     * @return the line under construction
     */
    public Line stack(StackExpression first, StackExpression... xStack) {
        this.xStack = Stream.concat(Stream.of(first), Arrays.stream(xStack)).collect(Collectors.toList());
        return this;
    }

    /**
     * Sets a condition to match the complete output exactly.
     * <p>
     *
     * @param output
     *            the expected output
     * @return the Line under construction
     */
    public Line output(String output) {
        outputConditions.add(s -> Assertions.assertThat(s).isEqualTo(output));
        return this;
    }

    /**
     * Sets a condition to match the tail of the output.
     *
     * @param output
     *            a string the output should end with
     * @return the Line under construction
     */
    public Line outputEndsWith(String output) {
        outputConditions.add(s -> Assertions.assertThat(s).endsWith(output));
        return this;
    }

    /**
     * Adds a condition to be checked after execution.
     *
     * @param condition
     *            the condition to add
     * @return the Line under construction
     */
    public Line check(Consumer<JemEngine> condition) {
        conditions.add(condition);
        return this;
    }

    @Override
    public Line ignore() {
        ignore = true;
        return null;
    }

    // access methods

    /**
     * Returns the forth command string to be executed.
     * <p>
     *
     * @return the command to be executed
     */
    protected String command() {
        return cmd;
    }

    /**
     * Execute all output conditions.
     *
     * @param actualOutput
     *            the output to check
     */
    void checkOutput(String actualOutput) {
        outputConditions.forEach(c -> c.accept(actualOutput));
    }

    /**
     * Checks the steck content.
     *
     * @param engine
     *            the engine whose stack is to be checkt
     */
    void checkStack(JemEngine engine) {
        assertThat(engine.getDataStack()).containsExactly(expectedStack(engine));
    }

    /**
     * Returns the expected stack content after command execution.
     * <p>
     *
     * @param engine
     *            the engine under test
     * @return the stack content, might be empty but not {@code null}.
     */
    private Integer[] expectedStack(JemEngine engine) {
        return !xStack.isEmpty() //
                ? evaluateStackExpressions(engine).toArray(new Integer[0])
                : stack;
    }

    private List<Integer> evaluateStackExpressions(JemEngine engine) {
        return xStack.stream().map(f -> f.apply(engine)).collect(Collectors.toList());
    }

    /**
     * Checks the configured post execution conditions
     * <p>
     *
     * @param engine
     *            the engine under test
     */
    void check(JemEngine engine) {
        conditions.forEach(c -> c.accept(engine));
    }

    @Override
    public boolean toBeExecuted() {
        return !ignore;
    }

    /**
     * {@inheritDoc}
     *
     * Used by the test framework as display name.
     */
    @Override
    public String toString() {
        return cmd;
    }
}
