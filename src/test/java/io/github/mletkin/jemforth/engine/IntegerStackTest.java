package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import io.github.mletkin.jemforth.engine.exception.EmptyStackException;

public class IntegerStackTest {

    IntegerStack stack = new IntegerStack();

    @Test
    void emptyStackHasDepthZero() {
        assertThat(stack.depth()).isZero();
    }

    @Test
    void pushIncrementsDepth() {
        stack.push(10);
        assertThat(stack.depth()).isEqualTo(1);
    }

    @Test
    void peekGetsTheRightValue() {
        stack.push(1);
        stack.push(2);
        stack.push(3);

        SoftAssertions.assertSoftly(bunch -> {
            bunch.assertThat(stack.peek(0)).isEqualTo(3);
            bunch.assertThat(stack.peek(1)).isEqualTo(2);
            bunch.assertThat(stack.peek(2)).isEqualTo(1);
        });
    }

    @Test
    void iPeekGetsTheRightValue() {
        stack.push(1);
        stack.push(2);
        stack.push(3);

        SoftAssertions.assertSoftly(bunch -> {
            bunch.assertThat(stack.iPeek(0)).isEqualTo(3);
            bunch.assertThat(stack.iPeek(1)).isEqualTo(2);
            bunch.assertThat(stack.iPeek(2)).isEqualTo(1);
        });
    }

    @Test
    void swapSwapsTheTopTwoElements() {
        stack.push(1);
        stack.push(2);
        stack.swap();
        assertThat(stack.stream()).containsExactly(2, 1);
    }

    @Test
    void swaOnEmptyStackThrowsException() {
        assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class).isThrownBy(stack::swap);
    }

    @Test
    void swaOnStackWithOneElementThrowsException() {
        stack.push(1);
        assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class).isThrownBy(stack::swap);
    }

    @Test
    void doubleAsInteger() {
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
    void popOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::pop);
    }

    @Test
    void peekOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(() -> stack.peek(0));
    }

    @Test
    void iPopOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::iPop);
    }

    @Test
    void iPeekOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(() -> stack.iPeek(0));
    }

    @Test
    void uPopOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::uPop);
    }

    @Test
    void dPopOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::dPop);
    }

    @Test
    void rollOnEmptyStack() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(() -> stack.roll(0));
    }

    @Test
    void rollFirstDoesNothing() {
        stack.push(1);
        stack.roll(0);
        assertThat(stack.iPop()).isEqualTo(1);
        assertThat(stack).isEmpty();
    }

    @Test
    void rollTest() {
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

    @Test
    void truePushesMinusOne() {
        stack.push(true);
        assertThat(stack.pop()).isEqualTo(-1);
    }

    @Test
    void falsePushesZero() {
        stack.push(false);
        assertThat(stack.pop()).isEqualTo(0);
    }

    @Test
    void cPushPushesCharacter() {
        stack.cPush('A');
        assertThat(stack.pop()).isEqualTo(65);
    }

    @Test
    void cPushNullPushesMinusOne() {
        stack.cPush(null);
        assertThat(stack.pop()).isEqualTo(-1);
    }

    @Test
    void cPopGetsCharThatcPushPushed() {
        stack.cPush('A');
        assertThat(stack.cPop()).isEqualTo('A');
    }

    @Test
    void cPopGetsUmlautCharThatcPushPushed() {
        stack.cPush('Ä');
        assertThat(stack.cPop()).isEqualTo('Ä');
    }

    @Test
    void numberPushWithIntegerPushesInteger() {
        stack.push((Number) (int) 4711);
        assertThat(stack.pop()).isEqualTo(4711);
    }

    @Test
    void uPushPushesLower32Bit() {
        final long value = 20_237_885_903_463L;
        stack.uPush(value);
        assertThat(stack.pop()).isEqualTo(4711);
    }

    @Test
    void long32BitValueIsPoppedAsUnsigned32BitInteger() {
        final long value = 2_147_488_359L;
        stack.uPush(value);

        assertThat(stack.depth()).isEqualTo(1);
        assertThat(stack.uPop()).isEqualTo(value);
    }
}
