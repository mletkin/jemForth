/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine.words;

import java.util.stream.Stream;

import io.github.mletkin.jemforth.Const;
import io.github.mletkin.jemforth.Package;
import io.github.mletkin.jemforth.engine.Command;
import io.github.mletkin.jemforth.engine.JemEngine;
import io.github.mletkin.jemforth.engine.MemoryMapper;
import io.github.mletkin.jemforth.engine.Util;
import io.github.mletkin.jemforth.engine.exception.NotByteAlignedException;
import io.github.mletkin.jemforth.engine.exception.NotCellAlignedException;

/**
 * Representation of a (generic) FORTH dictinary entry.
 * <p>
 * The classes implementing jemForth words are responible for for memory access.
 * This makes it possible to implement words that access data outside the engine
 * (like the {@code UserVariableWord}) or perform special memory mapping (like
 * the {@code StringWord}).<br>
 * Memory access may be cell oriented ({@link #store(int, Integer)} and
 * {@link #cStore(int, int)}) or byte oriented ({@link #fetch(int)} and
 * {@link #cFetch(int)}).<br>
 * The memory accessing methods are used by the definition of the forth words
 * {@code !}, {@code @}, {@code C!} and {@code C@}.
 * <ul>
 * <li>the xt is the identifier for the word
 * <li>the cfa holds a lambda expression to be executed when interpreted
 * <li>the address for the parameter fields is a memory locator
 * <li>the comment field is not forth standard and used by SEEE
 * <li>The access to the parameter area via pfa and index is directory specific
 * <li>The name is set on creation and immutable
 * </ul>
 */
public class Word {

    /**
     * Name of the word.
     */
    private final String name;

    /**
     * True if the word is immediate.
     */
    protected boolean immediate = false;

    /**
     * True if the word is hidden.
     */
    protected boolean hidden = false;

    /**
     * Vocabulary to which the word belongs (0 is "FORTH").
     */
    public Integer vocabulary = 0;

    /**
     * Technically the memory locator where the word starts logically.
     */
    protected Integer xt = null;

    /**
     * The lambda expression to be executed in interpretation state.
     * <p>
     * The default behavior is pushing the address of the first pfa field on the
     * data stack The Parameter area, starts -- logically -- one cell after the xt
     */
    public Command cfa = c -> c.push(firstPfaField());

    /**
     * Non Forth standard comment for use by the IDE.
     */
    protected String comment;

    /**
     * The memory mapper used for memory access.
     */
    protected MemoryMapper mm;

    /**
     * Creates a new word.
     *
     * @param name
     *                 the name of the word
     */
    public Word(String name) {
        this.name = name;
    }

    /**
     * Sets the memoryMapper
     *
     * @param mm
     *               the MemoryMapper to use
     */
    @Package(cause = "used only by dictionary")
    void useMemoryMapper(MemoryMapper mm) {
        this.mm = mm;
    }

    /**
     * Sets the word's access token.
     *
     * @param xt
     *               the xt to set
     */
    @Package(cause = "used only by dictionary")
    void setXt(Integer xt) {
        this.xt = xt;
    }

    /**
     * Returns the address of the first pfa field.
     *
     * @return the address
     */
    protected int firstPfaField() {
        return mm.xtToPfa(xt);
    }

    /**
     * Execution of the code field content without direct cfa access.
     *
     * @param context
     *                    Engine as context for execution
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
     * Returns true, if the word is hidden.
     *
     * @return the hidden {@code true} if hidden
     */
    public boolean isHidden() {
        return hidden;
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
     *                        array of comment lines to set
     * @return the word instance under construction
     */
    public Word comment(String... commentList) {
        comment = Util.stream(commentList).collect(Const.crSeparatedList());
        return this;
    }

    /**
     * Allocates space for a cell and store the value there.
     *
     * @param value
     *                  value to store
     */
    public void addPfaEntry(Integer value) {
        throw new NotCellAlignedException(this);
    }

    /**
     * Reads the cell content at the position defined by the locator.
     *
     * @param locator
     *                    absolute address of the integer value
     * @return function only supported for cell aligned words
     */
    public Integer fetch(int locator) {
        throw new NotCellAlignedException(this);
    }

    /**
     * Reads the byte at the position defined by the locator.
     *
     * @param locator
     *                    absolute address of the byte value
     * @return function only supported for cell aligned words
     */
    public int cFetch(int locator) {
        throw new NotByteAlignedException(this);
    }

    /**
     * Writes an integer value into a parameter cell.
     *
     * @param locator
     *                    absolute address of the cell
     * @param value
     *                    value to store in the cell
     */
    public void store(int locator, Integer value) {
        throw new NotCellAlignedException(this);
    }

    /**
     * Writes a byte value into a parameter cell.
     *
     * @param locator
     *                    absolute address of the byte
     * @param value
     *                    value to store in the byte
     */
    public void cStore(int locator, int value) {
        throw new NotByteAlignedException(this);
    }

    /**
     * Gets the number of memory cells allocated for the word.
     *
     * @return the number of allocated cells
     */
    public int cellCount() {
        return 0;
    }

    /**
     * Gets the parameter cells as Stream (used for inspector access).
     *
     * @return the content of the parameter area
     */
    public Stream<Integer> getDataArea() {
        return Stream.empty();
    }

    /**
     * Gets the word's access token.
     *
     * @return the xt
     */
    public Integer xt() {
        return xt;
    }

    @Override
    public String toString() {
        return name + "[" + xt + "]";
    }

}
