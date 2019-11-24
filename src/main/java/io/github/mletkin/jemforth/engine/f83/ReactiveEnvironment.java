/**
 * The JemForth project
 *
 * (C) 2018 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine.f83;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

import io.github.mletkin.jemforth.engine.JemEngine;

import java.util.concurrent.SubmissionPublisher;

/**
 * Glue class between a {@code JConsole} and a {@code JemForth} engine using reactive streams.
 */
public class ReactiveEnvironment extends ConsoleEnvironment {

    private BufferedSubscriber<Character> keyboard = new BufferedSubscriber<>();
    private SubmissionPublisher<Character> output = new SubmissionPublisher<>();

    /**
     * Connects a JemForth engine with reactive I/O channels.
     *
     * @param engine
     *            the Forth engine to connectx
     */
    public ReactiveEnvironment(JemEngine engine) {
        super(engine);
    }

    /**
     * Connect a publisher as input channel to act as keyboard.
     *
     * @param inputChannel
     *            the publisher to use as input channel
     */
    public void connectKeyboard(Publisher<Character> inputChannel) {
        inputChannel.subscribe(keyboard);
    }

    /**
     * Connect a subscriber as output channel to act as display.
     *
     * @param outputChannel
     *            the subscriber to attach to the output channel
     */
    public void connectDisplay(Subscriber<Character> outputChannel) {
        output.subscribe(outputChannel);
    }

    @Override
    protected synchronized char key() {
        keyboard.request(1);
        while (keyboard.getBuffer().isEmpty()) {
            ;
        }
        return keyboard.getBuffer().elementAt(0);
    }

    @Override
    protected char readChar() {
        return this.key();
    }

    @Override
    protected boolean isCharAvailable() {
        return !keyboard.getBuffer().isEmpty();
    }

    @Override
    public void print(String str) {
        for (int n = 0; n < str.length(); n++) {
            output.submit(str.charAt(n));
        }
    }

    @Override
    public void print(char character) {
        output.submit(character);
    }

}
