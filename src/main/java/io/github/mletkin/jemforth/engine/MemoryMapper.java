package io.github.mletkin.jemforth.engine;

/**
 * Manages memory IDs for an engine and helps to map memory locators.
 * <p>
 * An instance of the Mapper ensures, that all memory allocated by the dictionary can be accessed
 * through a linear address space. The allocated memory is accessed in the order of allocation and
 * clustered by the word. The {@code MemoryMapper} does no memory allocation. Is only counts the
 * number of allocated words and makes sure that every word gets a uniquie identifier.
 * <p>
 * The smallest memory unit is byte so you might say jemForth emulates an 8bit system.<br>
 * A cell is a 16 bit or 2 byte memory unit.<br>
 * An address locator is a 32 bit or 4 byte word. So the memory size is at most 2GB.<br>
 * Since Java has no 32bit unsigned integer type the most significant bit contains the sign. It
 * cannot be ignored because of the two's complement representation. A change in sign would not only
 * the sign bit but also additional bits making the bit masking impossibmle.<br>
 * So we emulate unsigned integer by not using negative numbers reducing the memory size to 1GB.
 * This is more than 32.000 times more a 8bit system offers -- and should be sufficient.
 * <p>
 * The (combined) locator or address locator of a memory unit is a 32bit word:<br>
 * 0www wwww wwww wwww pppp pppp pppp ppbb<br>
 * It consists of three sections (lsb is 1, msb is 32):
 * <ul>
 * <li>bit 17-31 The word identifier
 * <li>bit 03-16 The cell in the parameter area of the word
 * <li>bit 01-02 The byte in a cell
 * </ul>
 * <ul>
 * <li>A 32bit {@code locator} is an absolute address that identifies a word, a cell or a byte. It
 * always includes the word identifier. So the word an address belongs to can be derived directy
 * from the adddress locator.</li>
 *
 * <li>The {@code word identifier} identifies a word in the memory space. Usually it is used as
 * {@code xt} (see below) and not as the 16 bit section from the locator.</li>
 *
 * <li>The special {@code word identifier} zero marks the block buffer area. So the block buffers
 * may occupy a maximal area of 2^16 bytes or 64MB.</li>
 *
 * <li>A {@code position} is a relative address that identifies a cell in a cell aligned word or a
 * byte in a byte aligned word. It never includes the word but allways the cell.</li>
 *
 * <li>An {@code execution token} or {@code xt} technically defines the locator of the word. It is a
 * locator with the word identifier set and all other bits set to zero.</li>
 *
 * </ul>
 */
public class MemoryMapper {

    // number of bits used for pfa and byte locator
    private static final int WORD_OFFSET_IN_BITS = 16;

    // number of bits used for the byte specifier
    private static final int CELL_BITS = 2;

    // Mask to get the byte specifier from a locator
    private static final int BYTE_MASK = (1 << CELL_BITS) - 1;

    // number of memory units (bytes) per cell
    public static final int CELL_SIZE = 1 << CELL_BITS;

    // Mask to get the position specifier
    private static final int POSITION_MASK = 0xFFFF;

    // Mask to get the word specifier (aka xt)
    private static final int WORD_MASK = 0xFFFF0000;

    // shift WORD_OFFSET_IN_BITS bits left to get the (base) locator of the next word's xt
    // counts the number of allocated words
    private int ptr = 1;

    /**
     * Get the locator for the next free word.
     * <p>
     * TODO: should be synchronized
     *
     * @return the first byte of the first cell in the next available word section.
     */
    public int getNextMemoryLocator() {
        return ptr++ << WORD_OFFSET_IN_BITS;
    }

    /**
     * Extract the xt of a word definition from the locator
     *
     * @param locator
     *            locator containing the xt
     * @return execution token
     */
    public static int toXt(int locator) {
        return locator & WORD_MASK;
    }

    /**
     * Extract the position of a cell in a word definition from a locator.
     * <p>
     * specifies the cell in the PFA, first cell has position 0.
     *
     * @param locator
     *            locator containing the cell position
     * @return the cell position
     */
    public static int toCellPosition(int locator) {
        return (locator & POSITION_MASK) >> CELL_BITS;
    }

    /**
     * Get the memory locator specified by xt, cell number and byte position.
     *
     * @param xt
     *            execution token
     * @param cell
     *            number of the cell in the word
     * @param bytePosition
     *            position of the byte in the cell
     * @return the combined locator
     */
    public static int toLocator(int xt, int cell, int bytePosition) {
        return xt + (cell << CELL_BITS) + bytePosition;
    }

    /**
     * Get the cell locator containing the addressed byte.
     *
     * @param byteLocator
     *            locator addressing a byte
     * @return locator addressing a cell
     */
    public static int toCellLocator(int byteLocator) {
        return byteLocator & ~BYTE_MASK;
    }

    /**
     * Get the number of the byte in a cell (including neither word nor cell).
     *
     * @param byteLocator
     *            locator addressing a byte
     * @return position of a byte in a cell
     */
    public static int toByte(int byteLocator) {
        return byteLocator & BYTE_MASK;
    }

    /**
     * Get the position of a byte in a word (including the cell but not the word).
     *
     * @param address
     *            combined address locator
     * @return extracted address in the word
     */
    public static int toBytePosition(int address) {
        return address & POSITION_MASK;
    }

    /**
     * Get one of the bytes from a multi byte integer value.
     *
     * @param cellContent
     *            integer value representing the content of a cell
     * @param bytePosition
     *            0..3 from 0 being the low byte
     * @return the extracted byte
     */
    public static int extractByte(int cellContent, int bytePosition) {
        return (cellContent >> (8 * bytePosition)) & 0xFF;
    }

    /**
     * Set the byte in a multi byte integer value.
     *
     * @param intValue
     *            32 bit integer value to be manipulated
     * @param bytePosition
     *            0..3 from 0 being the low byte
     * @param byteValueToSet
     *            contains the value to set in the low byte
     *
     * @return the modified integer value
     */
    public static int setByte(int intValue, int bytePosition, int byteValueToSet) {
        long mask = (~0L) ^ (0xFF << (bytePosition * 8));
        long shiftedValue = (byteValueToSet & 0xFF) << (bytePosition * 8);
        return (int) ((intValue & mask) | shiftedValue);
    }

    /**
     * Is the address locator inside a block buffer?
     *
     * @param locator
     *            combined locator
     * @return {@code true} iff the locator specifies a block buffer
     */
    public static boolean isBufferAddress(int locator) {
        return (locator >> WORD_OFFSET_IN_BITS) == 0;
    }

    /**
     * Remove the number of the block buffer from the locator.
     *
     * @param locator
     *            combined locator
     * @return locator with the block buffer identifier removed
     */
    public static int toBufferOffset(int locator) {
        return locator & ~WORD_MASK;
    }

    /**
     * Address of the word following the word with the given locator.
     *
     * @param locator
     *            locator of the word that precedes the word searched
     * @return the locator identifying the word searched
     */
    public static int following(int locator) {
        return (locator & WORD_MASK) + (1 << WORD_OFFSET_IN_BITS);
    }
}
