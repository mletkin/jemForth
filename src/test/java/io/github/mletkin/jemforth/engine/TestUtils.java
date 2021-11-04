package io.github.mletkin.jemforth.engine;

import io.github.mletkin.jemforth.engine.words.Dictionary;
import io.github.mletkin.jemforth.engine.words.Word;

/**
 * Utility methods for unit tests.
 */
public final class TestUtils {

    private TestUtils() {
        // prevent instantiation
    }

    public static JemEngine mkEngine() {
        return new JemEngine(new Dictionary(new MemoryMapper(10)));
    }

    public static JemEngine mkEngineAddWord(Word word) {
        JemEngine engine = mkEngine();
        engine.add(word);
        return engine;
    }
}
