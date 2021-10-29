/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

/**
 * Representation of a colon definition word.
 */
public class ColonWord extends CellListWord {

    /**
     * Creates a new Word.
     */
    public ColonWord() {
        cfa = c -> c.docol(this.xt + MemoryMapper.CELL_SIZE);
    }
}
