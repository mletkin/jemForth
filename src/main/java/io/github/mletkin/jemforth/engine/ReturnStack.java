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
 * Currently, this is a wrapper for a Java Integer-Stack to prevent uncontrolled access. <br>
 * Unfortunately the IDE needs access to the object list that holds the content for display.
 */
public class ReturnStack {

    private Stack<Integer> stack = new Stack<>();

    public void push(Integer value) {
        stack.push(value);
    }

    public Integer pop() {
        return stack.pop();
    }

    public void clear() {
        stack.clear();
    }

    public Stream<Integer> stream() {
        return stack.stream();
    }

    public Integer peek(int n) {
        return stack.get(stack.size() - n);
    }

    public int depth() {
        return stack.size();
    }

    public List<Integer> content() {
        return stack;
    }

}
