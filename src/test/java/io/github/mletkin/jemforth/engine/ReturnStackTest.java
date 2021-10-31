package io.github.mletkin.jemforth.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.util.EmptyStackException;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class ReturnStackTest {

    ReturnStack stack = new ReturnStack();

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
    void popEmptiesStack() {
        stack.push(1);
        assumeThat(stack.depth()).isEqualTo(1);
        stack.pop();
        assertThat(stack.depth()).isZero();
    }

    @Test
    void popOnEmptyStackThrowsException() {
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::pop);
    }

    @Test
    void popGetsPushedData() {
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertThat(stack.pop()).isEqualTo(3);
        assertThat(stack.pop()).isEqualTo(2);
        assertThat(stack.pop()).isEqualTo(1);
    }

    @Test
    void clearEmptiesTheStack() {
        stack.push(1);
        stack.push(2);
        stack.push(3);

        assumeThat(stack.depth()).isEqualTo(3);
        stack.clear();
        assertThat(stack.depth()).isZero();
    }

    @Test
    void streamGetsStackContent() {
        stack.push(1);
        stack.push(2);
        stack.push(3);

        assertThat(stack.stream()).containsExactly(1, 2, 3);
    }

    @Test
    void peekOnEmptyStackThriwsException() {
        assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class).isThrownBy(() -> stack.peek(0));
    }

    @Test
    void peekGetsTheRightValue() {
        stack.push(1);
        stack.push(2);
        stack.push(3);

        SoftAssertions.assertSoftly(bunch -> {
            bunch.assertThat(stack.peek(1)).isEqualTo(3);
            bunch.assertThat(stack.peek(2)).isEqualTo(2);
            bunch.assertThat(stack.peek(3)).isEqualTo(1);
        });
    }

    @Test
    void contentGetsAlwaysTheSameListObject() {
        List<Integer> one = stack.content();
        stack.push(1);
        List<Integer> two = stack.content();

        assertThat(one).isNotNull();
        assertThat(one).isSameAs(two);
    }

}
