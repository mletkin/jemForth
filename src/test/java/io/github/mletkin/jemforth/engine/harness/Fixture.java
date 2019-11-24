/**
 * The JemForth project
 *
 * (C) 2018 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine.harness;

import java.util.Objects;
import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.JemEngine;

/**
 * A vise for a Forth engine to perform single and multi line tests.
 */
public class Fixture<E extends JemEngine> {

    private E engine;
    private OutputBuffer buffer;

    /**
     * Always use the factory method to create the fixture.
     * <p>
     *
     * @param <T>
     *            The Type of the engine embedded in the fixture
     *
     * @param engine
     *            The Engine under test
     */
    private Fixture(E engine) {
        this.engine = engine;
        buffer = new OutputBuffer(engine);
    }

    /**
     * Createa a new fixture for an engine.
     * <p>
     *
     * @param <T>
     *            Type of the engine to embed in the fixture.
     * @param engine
     *            the engine under test
     * @return A new fixture wrapped around the engine
     */
    public static <T extends JemEngine> Fixture<T> fixture(T engine) {
        return new Fixture<T>(engine);
    }

    /**
     * Test a single line of a program.
     * <p>
     * Executes the command contained in the {@link Line} object and and checks all of the configured conditions. The
     * Line might be part of a {@link Program} object.
     * <p>
     *
     * @param line
     *            a single line program to test
     */
    public void test(Line line) {
        String output = execute(line.command());
        line.checkOutput(output);
        line.checkStack(engine);
        line.check(engine);
    }

    /**
     * Tests a multi line program.
     * <p>
     * Each line of the program is executed with {@link Fixture#test(Line)} and all configured conditions are checked.
     * All lines are executed in the configured order with the same engine without any interference between the
     * executions.
     *
     * @param program
     *            a multi line Program to test
     */
    public void test(Program program) {
        program.lines().forEach(this::test);
    }

    /**
     * Executes a command without checking any condition and returns the collected output.
     * <p>
     *
     * @param command
     *            the forth coammand to execute
     * @return the output of the forth engine
     */
    public String execute(String command) {
        buffer.reset();
        engine.process(command);
        return buffer.content();
    }

    /**
     * Allows access to the embedded forth engine.
     * <p>
     *
     * @return the engine embedded in the fixture
     */
    public E engine() {
        return engine;
    }

    /**
     * Filters a Stream for the test cases to be executed.
     * <p>
     * Returns all test cases that are not {@code null} and that have not been marked with {@link Ignorable#ignore()}.
     *
     * @param <T>
     *            the type of the testcases, must extend Ignorable for filtering
     * @param testList
     *            list of test cases to filter
     * @return the filtered list
     */
    public static <T extends Ignorable<?>> Stream<T> testCaseList(T... testList) {
        return Stream.of(testList).filter(Objects::nonNull).filter(Ignorable::toBeExecuted);
    }

}
