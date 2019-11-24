/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

/**
 * Internal implementation of a one-word-variable.
 * <p>
 * TODO: throw ex on access pfa != xt + MemoryMapper.CELL_SIZE
 */
public class VariableWord extends Word {

    Integer value;

    @Override
    public Integer fetch(int pfa) {
        return value;
    }

    @Override
    public void store(int pfa, Integer value) {
        this.value = value;
    }

}
