/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.exception.IllegalMemoryAccessException;

/**
 * Dictionary word with a cell list as parameter field.
 * <p>
 * The basis of all cell aligned words.
 */
public class CellListWord extends Word {

    /**
     * Keeps the cells stored in this word.
     */
    private final List<Integer> parameter = new ArrayList<>();

    /**
     * Creates a new cell list word.
     *
     * @param name
     *                 the name of the word
     */
    public CellListWord(String name) {
        super(name);
    }

    /**
     * Adds a cell to the parameter area, perform comma/allot.
     *
     * @param value
     *                  value to add
     */
    @Override
    public void addPfaEntry(Integer value) {
        parameter.add(value);
    }

    /**
     * Reads the content of a parameter cell.
     *
     * @param pfa
     *                absolute address of the cell pfa = xt + [1..data.size()]
     * @return content of the parameter cell
     */
    @Override
    public Integer fetch(int pfa) {
        return parameter.get(mm.toCellPosition(pfa) - 1);
    }

    /**
     * Fetches the content of a byte referenced by the given address.
     *
     * @param byteLocator
     *                        absolute logic memory address
     * @return value found at address
     */
    @Override
    public int cFetch(int byteLocator) {
        Integer cellContent = fetch(mm.toCellLocator(byteLocator));
        int bytePosition = mm.toByte(byteLocator);
        return mm.extractByte(cellContent != null ? cellContent.intValue() : 0, bytePosition);
    }

    /**
     * Sets the content of a parameter cell.
     *
     * @param pfa
     *                  absolute address of the cell: pfa + [1..data.size()]
     * @param value
     *                  value to store in the cell
     */
    @Override
    public void store(int pfa, Integer value) {
        int index = mm.toCellPosition(pfa) - 1;
        if (index < 0) {
            throw new IllegalMemoryAccessException();
        }
        while (index >= parameter.size()) {
            parameter.add(null);
        }
        parameter.set(index, value);
    }

    /**
     * Stores a byte at the address specified by the given locator.
     *
     * @param locator
     *                    locator logic memory address
     * @param value
     *                    byte value to store
     */
    @Override
    public void cStore(int locator, int value) {
        Integer cellContent = fetch(mm.toCellLocator(locator));
        int bytePosition = mm.toByte(locator);
        store(locator, mm.setByte(cellContent != null ? cellContent : 0, bytePosition, value));
    }

    @Override
    public int cellCount() {
        return parameter.size();
    }

    @Override
    public Stream<Integer> getDataArea() {
        return parameter.stream();
    }

}
