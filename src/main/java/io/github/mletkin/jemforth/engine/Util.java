/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import static java.util.Optional.ofNullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Some utilities to be used anywhere :-)
 */
public final class Util {

    private Util() {
        // prevent instantiation
    }

    /**
     * Checks the string for emptiness.
     *
     * @param str
     *                String to check
     * @return {@code true} iff the string is empty
     */
    public static boolean isEmpty(String str) {
        if (str != null) {
            for (int n = str.length(); n > 0; n--) {
                if (!Character.isWhitespace(str.charAt(n - 1))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Closes a {@link Closeable} without exception.
     *
     * @param door
     *                 Object to close
     */
    public static void closeSilently(Closeable door) {
        try {
            if (door != null) {
                door.close();
            }
        } catch (Exception e) {
            // just suppress the exception
        }
    }

    /**
     * Used by {@code readLine} as parameter type.
     */
    @FunctionalInterface
    public interface Quit {
        boolean yes();
    }

    /**
     * Reads a line from the piped input stream.
     *
     * @param in
     *                 stream to read from
     * @param quit
     *                 stop if yes() evaluates to true
     * @return the string read
     * @throws IOException
     *                         exection reading stream
     */
    public static synchronized String readLine(PipedInputStream in, Quit quit) throws IOException {
        String input = "";
        do {
            int available = in.available();
            if (available == 0) {
                break;
            }
            byte b[] = new byte[available];
            in.read(b);
            input = input + new String(b, 0, b.length);
        } while (!input.endsWith("\n") && !quit.yes());
        return input;
    }

    /**
     * Execute a {@link Consumer} on an Object if not null.
     *
     * @param <T>
     *                   Class of Object to be processed
     * @param object
     *                   Objcet to be processed
     * @param doMe
     *                   onsumer to execute
     */
    public static <T> void doIfNotNull(T object, Consumer<T> doMe) {
        ofNullable(object).ifPresent(doMe);
    }

    /**
     * Returns a Stream from the array without exception.
     *
     * @param <T>
     *                 the type of the list objects
     * @param list
     *                 array of Objects or null
     * @return a stream with all elements from {@code list}
     */
    public static <T> Stream<T> stream(T[] list) {
        return list == null ? Stream.empty() : Arrays.stream(list);
    }

    /**
     * Predicate for negation of predicates.
     */
    public static <P> Predicate<P> not(Predicate<P> predicate) {
        return predicate.negate();
    }

    /**
     * Iterator for iterating a List in reverse order.
     *
     * @param <E>
     */
    static private class ReverseIterator<E> implements Iterator<E> {

        private final List<E> list;
        private int pos;

        private ReverseIterator(List<E> list) {
            this.list = list;
            pos = list.size();
        }

        @Override
        public boolean hasNext() {
            return pos > 0;
        }

        @Override
        public E next() {
            return list.get(--pos);
        }
    }

    /**
     * Streams the elements of the list in reverse order.
     *
     * @param <T>
     *                 Type of the list objects
     * @param list
     *                 list to stream
     * @return stream of teh list in reverse order
     */
    public static <T> Stream<T> reverse(List<T> list) {
        Spliterator<T> split = Spliterators.spliterator(new ReverseIterator<T>(list), list.size(), 0);
        return StreamSupport.stream(split, false);
    }
}
