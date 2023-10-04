/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import java.util.Stack;

import io.github.mletkin.jemforth.engine.exception.EmptyStackException;

/**
 * A Stack holding {@link Integer}-Objects.
 *
 * <ul>
 * <li>the elements are numbered 0..size -1 from top to bottom
 * <li>the java.util.stack elements are mubered from bottom to top
 * <li>most significant word for 64b Numbers is pushed on top
 * <li>peek access starts at top of stack
 * <li>maps illegal access to a custom EmptyStackException
 * </ul>
 */
public class IntegerStack extends Stack<Integer> {

    /**
     * The number of elements on the stack.
     *
     * @return the number of elements on the stack
     */
    public int depth() {
        return size();
    }

    // normal Integer operations

    @Override
    public Integer pop() {
        try {
            return super.pop();
        } catch (ArrayIndexOutOfBoundsException | java.util.EmptyStackException e) {
            throw new EmptyStackException(e);
        }
    }

    /**
     * Returns an element without removing.
     *
     * @param pos
     *                number of the element to retrieve
     * @return the peeked value
     */
    public Integer peek(int pos) {
        try {
            return get(size() - pos - 1);
        } catch (ArrayIndexOutOfBoundsException | java.util.EmptyStackException e) {
            throw new EmptyStackException(e);
        }
    }

    @Override
    public Integer peek() {
        try {
            return super.peek();
        } catch (ArrayIndexOutOfBoundsException | java.util.EmptyStackException e) {
            throw new EmptyStackException(e);
        }
    }

    /**
     * Swaps the top two elements of the stack.
     */
    public void swap() {
        swap(0, 1);
    }

    /**
     * Swaps two arbitrary elements of the stack.
     *
     * @param n
     *              index of the first element
     * @param m
     *              index of the second element
     */
    public void swap(int n, int m) {
        Integer one = get(size() - n - 1);
        Integer two = get(size() - m - 1);
        set(size() - n - 1, two);
        set(size() - m - 1, one);
    }

    /**
     * Brings the nth element to the top.
     *
     * @param n
     *              index of the element to bring up
     */
    public void roll(int n) {
        try {
            super.push(remove(size() - n - 1));
        } catch (ArrayIndexOutOfBoundsException | java.util.EmptyStackException e) {
            throw new EmptyStackException(e);
        }
    }

    /**
     * Pushes a bool value as Integer.
     *
     * @param b
     *              bool value
     */
    public void push(boolean b) {
        push(b ? Integer.valueOf(-1) : Integer.valueOf(0));
    }

    // signed 32 bit int operations

    /**
     * Boxes and pushes an int value.
     *
     * @param value
     *                  the value to push
     */
    public void iPush(int value) {
        super.push(Integer.valueOf(value));
    }

    /**
     * Pops and unboxes an int value.
     *
     * @return the value from the stack
     */
    public int iPop() {
        return pop().intValue();
    }

    /**
     * Pops and unboxes an int value and converts it to a char.
     *
     * @return the vchar value from the stack
     */
    public char cPop() {
        return (char) pop().intValue();
    }

    /**
     * Converts and pushes a Character value on the stack.
     *
     * @param zch
     *                the char to push
     */
    public void cPush(Character zch) {
        iPush(zch != null ? zch.charValue() : -1);
    }

    /**
     * Gets the value from a position in the stack without removing.
     *
     * @param pos
     *                position on the stack
     * @return the value retrieved
     */
    public int iPeek(int pos) {
        try {
            return peek(pos).intValue();
        } catch (ArrayIndexOutOfBoundsException | java.util.EmptyStackException e) {
            throw new EmptyStackException(e);
        }
    }

    // unsigned 32 bit operations

    /**
     * Pushes the lower 32 bit of a long as unsigned integer.
     *
     * @param value
     *                  the value to push
     */
    public void uPush(long value) {
        iPush((int) (value & 0xffffffff));
    }

    /**
     * Pops the top value as 32 bit unsigned integer.
     *
     * @return the unsigned value as long
     */
    public long uPop() {
        try {
            return Integer.toUnsignedLong(iPop());
        } catch (ArrayIndexOutOfBoundsException | java.util.EmptyStackException e) {
            throw new EmptyStackException(e);
        }
    }

    // signed 64 bit operations

    /**
     * Push one Long as two Integers, high word last.
     *
     * @param value
     *                  value to push
     */
    public void dPush(long value) {
        push((int) (value & 0xFFFFFFFF));
        push((int) (value >>> 32));
    }

    /**
     * Pops two integer and combines them to a long, high word first.
     *
     * @return the popped value
     */
    public long dPop() {
        return ((long) iPop() << 32) | (iPop() & 0xFFFFFFFFL);
    }

    /**
     * Combined Integer/Long push.
     *
     * @param number
     *                   number to push
     */
    public void push(Number number) {
        if (number instanceof Integer intNumber) {
            push(intNumber);
        } else if (number instanceof Long longNumber) {
            dPush(longNumber);
        }
    }

}
