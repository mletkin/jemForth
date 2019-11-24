/**
 * The JemForth project
 *
 * (C) by the Big Shedder 2018
 */
package io.github.mletkin.jemforth.engine.harness;

import java.util.function.Function;

import io.github.mletkin.jemforth.engine.JemEngine;

@FunctionalInterface
public interface StackExpression extends Function<JemEngine, Integer> {

    static StackExpression constant(int n) {
        return e -> n;
    }
}
