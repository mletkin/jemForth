package io.github.mletkin.jemforth.engine.f83;

import static java.util.Optional.ofNullable;

import java.util.Stack;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

/**
 * Subscriber that Reads items from a publisher and stores them in a
 * {@link Stack}.
 *
 * @param <T>
 *                Type of the items to be received from the publisher.
 */
public class BufferedSubscriber<T> implements Subscriber<T> {

    private final Stack<T> buffer = new Stack<>();
    private Subscription subscription;

    /**
     * Start reading from the publisher.
     *
     * @param n
     *              the number of items to be received.
     */
    public void request(int n) {
        buffer.clear();
        ofNullable(subscription).ifPresent(x -> x.request(n));
    }

    /**
     * Gets the input buffer.
     *
     * @return the input buffer
     */
    public Stack<T> getBuffer() {
        return buffer;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public void onNext(T item) {
        buffer.add(item);
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void onComplete() {
        this.subscription = null;
    }

}
