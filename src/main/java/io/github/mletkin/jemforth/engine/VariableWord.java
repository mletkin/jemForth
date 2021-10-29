/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

/**
 * A variable that has one cell to hold integer coded data.
 * <p>
 * Since there is only one value in the word, the {@code pfa} parameter is
 * ignored. Actually it should be checked.
 *
 * TODO: throw ex on access pfa != xt + MemoryMapper.CELL_SIZE
 */
public class VariableWord extends Word {

    private Integer value;

    @Override
    public Integer fetch(int pfa) {
        return value;
    }

    @Override
    public void store(int pfa, Integer value) {
        this.value = value;
    }

}
