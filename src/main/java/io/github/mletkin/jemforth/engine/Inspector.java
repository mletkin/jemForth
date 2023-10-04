package io.github.mletkin.jemforth.engine;

import static io.github.mletkin.jemforth.Const.CR;
import static io.github.mletkin.jemforth.Const.SPACE;
import static io.github.mletkin.jemforth.engine.MemoryMapper.CELL_SIZE;
import static io.github.mletkin.jemforth.engine.Util.isEmpty;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.mletkin.jemforth.Const;
import io.github.mletkin.jemforth.engine.words.CellListWord;
import io.github.mletkin.jemforth.engine.words.ColonWord;
import io.github.mletkin.jemforth.engine.words.ConstantWord;
import io.github.mletkin.jemforth.engine.words.Dictionary;
import io.github.mletkin.jemforth.engine.words.InternalWord;
import io.github.mletkin.jemforth.engine.words.StringWord;
import io.github.mletkin.jemforth.engine.words.UserVariableWord;
import io.github.mletkin.jemforth.engine.words.VariableWord;
import io.github.mletkin.jemforth.engine.words.VocabularyWord;
import io.github.mletkin.jemforth.engine.words.Word;

/**
 * Debugging tools for dictionary insight.
 *
 * Numbers are formatted with the engine's base
 */
public class Inspector {

    /**
     * Function to get the dictionary to observe.
     */
    private Supplier<Dictionary> dict;

    /**
     * Function to format integer values.
     */
    private Function<Integer, String> formatter;

    /**
     * Create an Inspector for a dictionary.
     *
     * @param dict
     *                      function to get the dictionary
     * @param formatter
     *                      function to format integer values
     */
    public Inspector(Supplier<Dictionary> dict, Function<Integer, String> formatter) {
        this.dict = dict;
        this.formatter = formatter;
    }

    /**
     * 15.6.1.2465 WORDS ( -- )
     * <p>
     * List the definition names in the first word list of the search order.<br>
     * The format of the display is implementation-dependent.
     *
     * @return a string containing a list of word names separated by space
     */
    public String words() {
        Dictionary dictionary = dict.get();
        return dictionary.memory().stream() //
                .filter(w -> w.vocabulary.equals(dictionary.getContext())) //
                .filter(w -> !w.isHidden()) //
                .filter(w -> !isEmpty(w.name())) //
                .filter(w -> dictionary.find(w.name()) == w) //
                .map(Word::name) //
                .collect(Const.crSeparatedList()) + SPACE;
    }

    /**
     * 15.6.1.2194 SEE
     *
     * Display a human-readable representation of the named word's definition.
     *
     * @param word
     *                 the word to decompile
     * @return a string containing a text representation of the word's definition
     */
    public String see(Word word) {
        String desc = ofNullable(word).map(Word::getComment).map(s -> s + CR).orElse("");
        return CR + desc + getDescription(word) + SPACE;
    }

    private String getDescription(Word word) {
        CodeType type = CodeType.find(word);
        String qualifier = type.longName + SPACE;
        switch (type) {
        case NULL:
            return "word not found";
        case VOCABLULARY:
            return qualifier + word.name() + ": id " + ((VocabularyWord) word).getWordListIdentifier();
        case CONST:
            return qualifier + word.name() + ": " + word.fetch(word.xt());
        case INTERN:
            return qualifier + (word.isImmediate() ? "IMMEDIATE " : "") + word.name();
        case COLON:
        case CELLLIST:
            return qualifier + word.name() + SPACE + wordList(word) + " ;" + (word.isImmediate() ? " IMMEDIATE " : "");
        case STR:
            return qualifier + word.name() + " [" + ((StringWord) word).data() + "]";
        default:
            return qualifier + word.name();
        }
    }

    /**
     * Gets a detailed description of a word's definition.
     *
     * @param word
     *                 the word to decompile
     * @return a string containing a detailed text representation of the word's
     *         definition
     */
    public String deCompile(Word word) {
        if (CodeType.COLON.is(word) || CodeType.CELLLIST.is(word)) {
            return CR + decompileWordList(word).stream().collect(Const.crSeparatedList()) + CR;
        }
        return see(word);
    }

    /**
     * Gets the decompiled parameter area as string list.
     *
     * @param word
     *                 the word to decompile
     * @return a string list with the decompiled definitions
     */
    public List<String> decompileWordList(Word word) {
        List<String> liste = new ArrayList<>();
        for (int n = 1, locator = word.xt() + CELL_SIZE; n <= word.cellCount(); n++, locator += CELL_SIZE) {
            Word subWord = dict.get().getByXt(word.fetch(locator));
            liste.add(formatSubWordEntry(word, locator, subWord));
            if (subWord != null && Inspectable.STRING_LITERAL.equals(subWord.name())) {
                n++;
                locator += CELL_SIZE;
                liste.add(formatStringReferenceEntry(locator, word.fetch(locator)));
            }
        }
        return liste;
    }

    /**
     * Formats a single subword as word reference.
     *
     * If the subword is not {@code null}, the subword's name is returned. Otherwise
     * the content of the locator's memory address is returned. The locator is
     * prepended as "line number".
     *
     * @param word
     *                    fallback, if subword is {@code null}
     * @param locator
     *                    locator acting as line number
     * @param subWord
     *                    subWord to format
     * @return the formatted subword
     */
    private String formatSubWordEntry(Word word, int locator, Word subWord) {
        return asString(locator) + SPACE
                + ofNullable(subWord).map(Word::name).orElseGet(() -> asString(word.fetch(locator)));
    }

    /**
     * Format subword as string literal.
     *
     * The subword is interpreted as {@link StringWord}. The result is a combination
     * of line number, address and string content.
     *
     * @param lineNumber
     *                       line number to be used as prefix
     * @param address
     *                       address containing the string to display
     * @return the formatted string literal
     */
    private String formatStringReferenceEntry(int lineNumber, int address) {
        return asString(lineNumber) + SPACE + asString(address) + ": " + getStringLiteralRepresentation(address);
    }

    private String getStringLiteralRepresentation(int address) {
        try {
            return asString(dict.get().findString(address));
        } catch (ClassCastException e) {
            return asString(dict.get().findWordContainingPfa(address).name());
        }
    }

    private String asString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof Integer integer) {
            return formatter.apply(integer);
        }
        return obj.toString();
    }

    private String wordList(Word word) {
        return word.getDataArea() //
                .map(value -> ofNullable(dict.get().getByXt(value)).map(Word::name).orElseGet(() -> asString(value))) //
                .collect(Collectors.joining(String.valueOf(SPACE)));
    }

    /**
     * Enumeration of the supported word types.
     */
    public enum CodeType {
        VOCABLULARY("voc", "vocabulary", VocabularyWord.class),
        USR("usr", "user variable", UserVariableWord.class),
        VAR("var", "variable", VariableWord.class),
        CONST("con", "constant", ConstantWord.class),
        INTERN("int", "internal", InternalWord.class),
        COLON("col", ":", ColonWord.class),
        STR("str", "String", StringWord.class),
        CELLLIST("cell", "cell list", CellListWord.class),
        GENERIC("gen", "generic word", Word.class),
        NULL("nul", "empty word", Word.class),

        ;

        private String shortName;
        private String longName;
        private Class<? extends Word> type;

        /**
         * Creates a code type enum object.
         *
         * @param shortName
         *                      short name
         * @param longName
         *                      long name
         * @param type
         *                      word class
         */
        CodeType(String shortName, String longName, Class<? extends Word> type) {
            this.shortName = shortName;
            this.longName = longName;
            this.type = type;
        }

        /**
         * Gets the short name of the type.
         *
         * @return sort name
         */
        public String shortName() {
            return shortName;
        }

        /**
         * Checks if a Word has the given {@code CodeType}.
         *
         * @param word
         *                 word to inspeect
         * @return {@code true} iff the word is of the given {@code CodeType}
         */
        public boolean is(Word word) {
            return this.type.isInstance(word);
        }

        /**
         * Finds the {@code CodeType} of a word.
         *
         * {@code null} has its own {@code CodeType}, unknown code types default to
         * {@code GENERIC}.
         *
         * @param word
         *                 word to inspect
         * @return the {@code CodeType} of the word.
         */
        public static CodeType find(Word word) {
            if (word != null) {
                return Stream.of(values()).filter(t -> t.type.isInstance(word)).findFirst().orElse(GENERIC);
            }
            return NULL;
        }
    }
}