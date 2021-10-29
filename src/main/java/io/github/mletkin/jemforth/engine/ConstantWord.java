package io.github.mletkin.jemforth.engine;

/**
 * Representation of a java implemented single cell CONSTANT word.
 *
 * This is a _real_ constant as the value is not alterable.<br>
 * Forth implementations of constants tend to be special variables.
 */
public class ConstantWord extends Word {

    /**
     * The constant value the word represents
     */
    private final Integer value;

    /**
     * Creates a new Word.
     *
     * @param value
     *                  the value the wirds represents
     */
    public ConstantWord(Integer value) {
        this.value = value;
        cfa = c -> c.stack.push(value);
    }

    @Override
    public Integer fetch(int pfa) {
        return value;
    }
}
