/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.mletkin.jemforth.engine.exception.NotByteAlignedException;
import io.github.mletkin.jemforth.engine.exception.NotCellAlignedException;

/**
 * Representation of a (generic) FORTH dictinary entry.
 *
 * <ul>
 * <li>the xt is the identifier for the word
 * <li>the cfa holds a lambda to be executed when interpreted
 * <li>the address for the parameter fields is a memory locator
 * <li>the comment field is not forth standard and used by SEEE
 * <li>The access to the parameter area via pfa and index is directory specific
 * </ul>
 */
public class Word {

    protected String name = null;
    protected boolean immediate = false;
    protected boolean hidden = false;
    public Integer vocabulary = 0;

    // Actually the memory locator where the word starts logically
    public Integer xt = null;

    // The lambda to be executed in interpretation state
    // The default behavior is pushing the address of the first pfa field
    // The Parameter area, starts -- logically -- one cell after the xt
    public Command cfa = c -> c.stack.push(xt + MemoryMapper.CELL_SIZE);

    // non Forth standard comment for use by the IDE
    protected String comment;

    /**
     * Execution of the code field content without direct cfa access.
     *
     * @param context
     *            Engine as context for execution
     */
    public final void execute(JemEngine context) {
        cfa.execute(context);
    }

    /**
     * Gets the name of the word.
     *
     * @return the name of the word
     */
    public String name() {
        return this.name;
    }

    /**
     * Gets the comment associated with the word.
     *
     * @return the comment associated with the word
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Checks whether the word is immediate.
     *
     * @return {@code true} iff the word is immediate
     */
    public boolean isImmediate() {
        return this.immediate;
    }

    /**
     * Builder style setter to make the word "immediate".
     *
     * @return the word instance under construction
     */
    public Word immediate() {
        this.immediate = true;
        return this;
    }

    /**
     * Builder style setter for the comment.
     *
     * @param commentList
     *            array of comment lines to set
     * @return the word instance under construction
     */
    public Word comment(String... commentList) {
        comment = Util.stream(commentList).collect(Collectors.joining("\n"));
        return this;
    }

    /**
     * Allocate space for a cell and store the value there.
     *
     * @param value
     *            value to store
     */
    public void addPfaEntry(Integer value) {
        throw new NotCellAlignedException(this);
    }

    /**
     * Read the cell content at the position defined by the locator.
     *
     * @param locator
     *            absolute address of the integer value
     * @return function only supported for cell aligned words
     */
    public Integer fetch(int locator) {
        throw new NotCellAlignedException(this);
    }

    /**
     * Read the byte at the position defined by the locator.
     *
     * @param locator
     *            absolute address of the byte value
     * @return function only supported for cell aligned words
     */
    public int cFetch(int locator) {
        throw new NotByteAlignedException(this);
    }

    /**
     * Write an integer value into a parameter cell.
     *
     * @param locator
     *            absolute address of the cell
     * @param value
     *            value to store in the cell
     */
    public void store(int locator, Integer value) {
        throw new NotCellAlignedException(this);
    }

    /**
     * Write a byte value into a parameter cell.
     *
     * @param locator
     *            absolute address of the byte
     * @param value
     *            value to store in the byte
     */
    public void cStore(int locator, int value) {
        throw new NotByteAlignedException(this);
    }

    /**
     * Get the number of allocated memory cells.
     *
     * @return the number of allocated cells
     */
    public int cellCount() {
        return 0;
    }

    /**
     * Get parameter cells as Stream (used for inspector access).
     *
     * @return the content of the parameter area
     */
    public Stream<Integer> getDataArea() {
        return Stream.empty();
    }

    @Override
    public String toString() {
        return name + "[" + xt + "]";
    }
}
