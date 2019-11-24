package io.github.mletkin.jemforth.engine;

/**
 * Representation of a java implemented single cell CONSTANT word.
 *
 * This is a _real_ constant as the value is not alterable.<br>
 * Forth implementations of constants tend to be special variables.
 */
public class ConstantWord extends Word {

    // the constant value for inspector access
    private Integer value;

    public ConstantWord(Integer value) {
        this.value = value;
        cfa = c -> c.stack.push(value);
    }

    @Override
    public Integer fetch(int pfa) {
        return value;
    }
}
