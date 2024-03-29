/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine.words;

import io.github.mletkin.jemforth.engine.MemoryMapper;
import io.github.mletkin.jemforth.engine.exception.IllegalStringLengthException;

/**
 * A word that stores a byte oriented dynamic length counted string.
 *
 * <ul>
 * <li>string is extended as needed but not reduced.
 * <li>strings are logically preceeded by a length byte.
 * <li>strings are byte aligned
 * <li>memory is allocated by {@link #cFetch}
 * <li>the length "byte" is 16bit (= parameter + byte identifier) long
 * </ul>
 */
public class StringWord extends Word {

    /**
     * Fill character used when extending the words space.
     */
    private static final char FILL = ' ';

    /**
     * The string data contained in the word.
     */
    private String data = "";

    {
        // runtime action: push address of length byte
        cfa = c -> c.push(xt + 1);
    }

    /**
     * Creates a new string word.
     *
     * @param name
     *                 name of the word
     */
    public StringWord(String name) {
        super(name);
    }

    /**
     * Sets the string word's content to "empty".
     */
    public void clear() {
        data = "";
    }

    /**
     * Fetches a single byte from the string.
     *
     * The first byte is the length.<br>
     * Because the byte position is extracted from the locator by masking,<br>
     * The integer representation of a character can be accessed by index, the range
     * is 1..data.length()
     *
     * TODO: convert from char to integer
     */
    @Override
    public int cFetch(int byteLocator) {
        if (byteLocator == xt + 1) {
            return this.length();
        }
        int position = mm.toBytePosition(byteLocator) - 2;
        return data != null && position < data.length() ? data.charAt(position) : 0;
    }

    /**
     * Store a single byte in the string, allocate memory if necessary.
     * <p>
     * changing the length byte will allocate memory
     */
    @Override
    public void cStore(int byteLocator, int value) {
        if (byteLocator == xt + 1) {
            setLength(value);
            return;
        }
        int position = mm.toBytePosition(byteLocator) - 2;
        data = data == null ? "" : data;
        StringBuffer buf = new StringBuffer("");
        for (int n = 0; n < position; n++) {
            buf.append(n < data.length() ? data.charAt(n) : FILL);
        }
        buf.append((char) (value & 0xFF));
        for (int n = position + 1; n < data.length(); n++) {
            buf.append(data.charAt(n));
        }
        data = buf.toString();
    }

    private void setLength(int value) {
        if (value < 0 || value >= 1 << 16) {
            throw new IllegalStringLengthException(value);
        }
        int diff = value - length();
        if (diff > 0) {
            StringBuffer buf = new StringBuffer(data);
            for (int n = 0; n < diff; n++) {
                buf.append(FILL);
            }
            data = buf.toString();
        } else {
            data = data.substring(0, value);
        }
    }

    /**
     * Returns the number of characters of the content string.
     *
     * @return the length of the contained string
     */
    public int length() {
        return data == null ? 0 : data.length();
    }

    @Override
    public int cellCount() {
        return (length() + 1) / MemoryMapper.CELL_SIZE;
    }

    @Override
    public void addPfaEntry(Integer value) {
        // prevent exception :-)
    }

    /**
     * Allocates a number of bytes.
     * <p>
     * The length byte is not allocated.
     *
     * @param n
     *              the number of bytes to allocate
     */
    public void allot(int n) {
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                data += FILL;
            }
        }
    }

    /**
     * Returns the stored content as Java string.
     *
     * @return the stored String
     */
    public String data() {
        return data;
    }

    /**
     * Replaces the stored content with a Java string.
     *
     * @param data
     *                 the new content
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Converts the integer to a character and add it to the String.
     * <p>
     * Used to add to pictured output
     *
     * @param character
     *                      integer representation of a character
     */
    public void prepend(int character) {
        data = (char) (character & 0xFF) + data;
    }

    /**
     * Gets the character by index.
     *
     * @param index
     *                  zero based index of the character.
     * @return the character at the position
     */
    public char charAt(int index) {
        return data.charAt(index);
    }

    @Override
    public String toString() {
        return data + "[" + xt + "]";
    }

}
