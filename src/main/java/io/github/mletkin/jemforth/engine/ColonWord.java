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

    {
        cfa = c -> c.docol(firstPfaField());
    }

    /**
     * Creates a new colon word.
     *
     * @param name
     *                 the name of the word
     */
    public ColonWord(String name) {
        super(name);
    }

}
