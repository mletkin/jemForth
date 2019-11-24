package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.IntegerStack;
import io.github.mletkin.jemforth.engine.exception.EmptyStackException;

public class IntegerStackTest {

    IntegerStack stack = new IntegerStack();

    @Test
    public void doubleAsInteger() {
        stack.dPush(1000000000000L);
        assertThat(stack.dPop()).isEqualTo(1000000000000L);

        stack.push(-1000000000000L);
        assertThat(stack.dPop()).isEqualTo(-1000000000000L);

        stack.push(4711L);
        assertThat(stack.dPop()).isEqualTo(4711L);

        stack.push(-1L);
        assertThat(stack.dPop()).isEqualTo(-1);
    }

    @Test
    public void popOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::pop);
    }

    @Test
    public void peekOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(() -> stack.peek(0));
    }

    @Test
    public void iPopOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::iPop);
    }

    @Test
    public void iPeekOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(() -> stack.iPeek(0));
    }

    @Test
    public void uPopOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::uPop);
    }

    @Test
    public void dPopOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::dPop);
    }

    @Test
    public void rollOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(() -> stack.roll(0));
    }

    @Test
    public void rollFirstDoesNothing() {
        stack.push(1);
        stack.roll(0);
        assertThat(stack.iPop()).isEqualTo(1);
        assertThat(stack).isEmpty();
    }

    @Test
    public void rollTest() {
        stack.push(4);
        stack.push(3);
        stack.push(2);
        stack.push(1);

        stack.roll(2);

        assertThat(stack.iPop()).isEqualTo(3);
        assertThat(stack.iPop()).isEqualTo(1);
        assertThat(stack.iPop()).isEqualTo(2);
        assertThat(stack.iPop()).isEqualTo(4);
    }
}
