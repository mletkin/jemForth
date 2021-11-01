package io.github.mletkin.jemforth;

import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Some often used constants.
 */
public final class Const {

    /**
     * Carriage return.
     */
    public static final char CR = '\n';

    /**
     * Single space.
     */
    public static final char SPACE = ' ';

    private Const() {
        // prevent instantiation
    }

    /**
     * Creates a collector that separates the lines by CRs.
     *
     * @return the collector
     */
    public static Collector<CharSequence, ?, String> crSeparatedList() {
        return Collectors.joining(String.valueOf(CR));
    }

    /**
     * Creates a collector that separates the lines by a single space.
     *
     * @return the collector
     */
    public static Collector<CharSequence, ?, String> spaceSeparatedList() {
        return Collectors.joining(String.valueOf(SPACE));
    }
}
