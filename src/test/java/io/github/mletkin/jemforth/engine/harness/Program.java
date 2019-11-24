/**
 * The JemForth project
 *
 * (C) by the Big Shedder 2018
 */
package io.github.mletkin.jemforth.engine.harness;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a multi line Forth program with execution conditions.
 * <p>
 * Each program consists of one or more {@link Line}-Objects. The program is
 * executed by a fixture line by line using one engine for all lines. After the
 * execution of each line the associated conditions are checked.
 * <p>
 * Programs are created by method chaining in fluent style.<br>
 * Properties might be added but not changed after definition.
 */
public final class Program implements Ignorable<Program> {

    private String name;
    private List<Line> lines = new ArrayList<>();
    private String word; // 2012 referencce of the word in test
    private boolean ignore = false; // ignore test case

    /**
     * Always use a factory method to instantiate new objects.
     * <p>
     *
     * @param name
     *            the name of the program
     */
    private Program(String name) {
        this.name = name;
    }

    // Content Definition

    /**
     * Factory method to construct a program with the given name.
     * <p>
     * The name cannot be changed
     *
     * @param name
     *            the name of the program
     * @return the newly constructed program
     */

    public static Program program(String name) {
        return new Program(name);
    }

    /**
     * Adds a {@code Line} object to the end of the list of lines.
     * <p>
     *
     * @param line
     *            the {@code Line} object to add
     * @return the program under construction
     */
    public Program add(Line line) {
        lines.add(line);
        return this;
    }

    @Override
    public Program ignore() {
        this.ignore = true;
        return this;
    }

    /**
     * experimental.
     *
     * @param word
     *            a free text
     * @return the program under construction
     */
    public Program ref(String word) {
        this.word = word;
        return this;
    }

    /**
     * Returns the reference for this word
     *
     * @return the reference word
     */
    public String ref() {
        return this.word;
    }

    // Access methods

    /**
     * The name of the program.
     * <p>
     *
     * @return the name of the program
     */
    public String name() {
        return name;
    }

    /**
     * The list of program lines.
     * <p>
     * The method is intended to be accessed by the fixture only, hence package
     * visible.
     *
     * @return a stream of lines in the order of execution
     */
    Stream<Line> lines() {
        return lines.stream();
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
        return name;
    }
}
