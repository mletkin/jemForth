/**
 * The JemForth project
 *
 * (C) 2018 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

/**
 * Implementation of the engines return stack.
 * <p>
 * Currently, this is a wrapper for a Java Integer-Stack to prevent uncontrolled
 * access. <br>
 * Unfortunately the IDE needs access to the object list that holds the content
 * for display.
 */
public class ReturnStack {

    /**
     * Internal representation of the stack.
     */
    private final Stack<Integer> stack = new Stack<>();

    /**
     * Pushes a single integer value.
     *
     * @param value
     *                  the value to push
     */
    public void push(Integer value) {
        stack.push(value);
    }

    /**
     * Pops a single integer value from the stack.
     *
     * @return the poped value
     */
    public Integer pop() {
        return stack.pop();
    }

    /**
     * Emptied the stack.
     */
    public void clear() {
        stack.clear();
    }

    /**
     * Gets all values on the Stack.
     *
     * @return Steam of integer values
     */
    public Stream<Integer> stream() {
        return stack.stream();
    }

    /**
     * Returns the value at position n without popping.
     *
     * @param n
     *              position on the stack from the top [1..depth]
     * @return value retrieved
     */
    public Integer peek(int n) {
        return stack.get(stack.size() - n);
    }

    /**
     * Gets the number of values in the stack.
     *
     * @return number of stack values
     */
    public int depth() {
        return stack.size();
    }

    /**
     * Access to the stack content.
     * <p>
     * Exposing the internal list is of cause not recommended. Unfortunately the
     * swing table components need acces to the list itself.<br>
     * One alternative woud be a second list that's synchronized with the "real"
     * list. The consequence would be a severe performance loss.<br>
     * Another alternative would be a mock list that allows access to the real list.
     *
     * @return The internal {@code List} object
     */
    public List<Integer> content() {
        return stack;
    }

}
