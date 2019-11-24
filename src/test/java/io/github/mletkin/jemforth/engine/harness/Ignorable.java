/**
 * The JemForth project
 *
 * (C) by the Big Shedder 2018
 */
package io.github.mletkin.jemforth.engine.harness;

/**
 * Interface for test cases that might be excluded from testing.
 *
 *
 * @param <T>
 *            Type of the test case
 */
public interface Ignorable<T> {

    /**
     * Called when the test case should be excluded from execution.
     *
     * @return The test case itself, allows fluent style method chaining
     */
    T ignore();

    /**
     * Called by the fixture to identify the test cases to execute.
     *
     * @return {@code true} if the test case should be executed.
     */
    boolean toBeExecuted();

}
