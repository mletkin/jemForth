package io.github.mletkin.jemforth.frontend;

import io.github.mletkin.jemforth.engine.console.reactive.F83ReactiveConsoleWindow;
import io.github.mletkin.jemforth.engine.f83.Forth83Engine;

/**
 * Running a Forth 83 engine with keyboard/display connected to a combined
 * console with reactive streams.
 */
public final class F83ConsoleReactive {

    private F83ConsoleReactive() {
        // prevent instantiation
    }

    /**
     * Read commands line by line until the user enters "bye".
     *
     * @param args
     *                 array with pogram arguments
     */
    public static void main(String[] args) {
        new F83ReactiveConsoleWindow(new Forth83Engine());
    }

}
