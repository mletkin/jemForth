/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine;

import static io.github.mletkin.jemforth.engine.MemoryMapper.CELL_SIZE;
import static io.github.mletkin.jemforth.engine.MemoryMapper.toLocator;
import static io.github.mletkin.jemforth.engine.Util.reverse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Forth Dictionary.
 * <p>
 * The Dictionary is the structure that contains all word definitions of a forth
 * engine. A Dictionary may contain multiple vocabularies to organize the words
 * defining a partition of the dictionary.<br>
 * Usually the words are referenced by name but to speed up interpretation the
 * dictionary keeps a map to access words by their execution token.<br>
 * For the same reason the vocabularies keep a list of the words contained in
 * the vocabulary.
 *
 * In a real forth system (real) memory is organized as a large random access
 * array with consecutive numbered cells. The JVM does not allow access to the
 * physical memory making all memory access virtual. As a consequence jemForth
 * emulates the linear address space making real memory access even more virtual
 * than the virtual memory access of the JVM...
 *
 * The reason for this bizarre concept is the attempt to
 * <ul>
 * <li>make it possible to access the memory as one linear space
 * <li>accelerate execution through maps
 * <li>implement memory mapping with simple bit manipulation
 * <li>use java objects to implement forth elements
 * </ul>
 *
 * The {@code Dictionary} class handles the memory access through the methods
 * {@code fetch}, {@code cFetch}, {@code store} and {@code cStore}. The actual
 * access to the data stored in the words is delegated to the individul words.
 * The address calculation is performed by the class {@code MemoryMapper}.
 * <ul>
 * <li>Every Dictionary is bound to a single forth engine.
 * <li>interface for word retrieval
 * <li>manages memory access
 * <li>memory is allocated in bytes and aligned by cells
 * </ul>
 */
public class Dictionary {

    // The word definitions ordered by creation/memory locator for serial access
    private final List<Word> memory = new ArrayList<>();

    // The word definitions accessible by xt for fast access by the interpreter
    private final Map<Integer, Word> byExecutionToken = new HashMap<>();

    // the word currently in compilation (aka LAST)
    private Word currentWord;

    // number of bytes allocated in the current Word (0..CELL_SIZE-1)
    private int bytesAllocated;

    // The first word that can not be deleted
    protected int fence = 0;

    protected MemoryMapper memoryMapper = new MemoryMapper();

    protected SearchResolver searchResolver = new SearchResolver();

    public SearchResolver getSearchResolver() {
        return searchResolver;
    }

    /**
     * Starts a new word definition of the given type and name.
     *
     * Used for internal defining words.
     *
     * @param word
     *                 word to add
     * @param name
     *                 the name of the word
     */
    public void create(Word word, String name) {
        currentWord = word;
        currentWord.name = name;
        bytesAllocated = 0;
        add(currentWord);
    }

    /**
     * Creates a new string word and makes it the current word.
     * <p>
     * The length byte and predefined content are allocated automatically.<br>
     * Characters may be allocated as needed.
     *
     * @param word
     *                 the string word to add.
     */
    public void create(StringWord word) {
        currentWord = word;
        bytesAllocated = 1 + word.length();
        add(currentWord);
    }

    /**
     * Adds a complete(ed) word to the dictionary.
     * <ul>
     * <li>computes the xt for the word
     * <li>adds the word to the current vocabulary
     * <li>Does not affect the {@link currentWord} or it's compilation.
     * </ul>
     *
     * @param word
     *                 the word to be added
     * @return the added word with the xt field set
     */
    public Word add(Word word) {
        word.xt = nextXt();
        memory.add(word);
        searchResolver.getCurrentVocabulary().add(word);
        byExecutionToken.put(word.xt, word);
        return word;
    }

    /**
     * Retrieves a word definition by name.
     *
     * @param name
     *                 name of the word wanted
     * @return the {@link Word} or @code null}
     */
    public Word find(String name) {
        return searchResolver.find(name);
    }

    /**
     * Retrieves a string by contained parameter field address.
     *
     * @param address
     *                    addess that contains the string
     * @return the content of the string word
     */
    public String findString(int address) {
        return ((StringWord) findWordContainingPfa(address)).data();
    }

    /**
     * Returns the xt the next word will get.
     *
     * @return the next available xt
     */
    private int nextXt() {
        return memoryMapper.getNextMemoryLocator();
    }

    /**
     * Returns the word currently in compilation.
     *
     * @return the word currently in compilation
     */
    public Word getCurrentWord() {
        return this.currentWord;
    }

    /**
     * Retrieves a Word by memory locator (xt).
     *
     * @param locator
     *                    locator of the wanted word
     * @return the word with the given xt
     */
    public Word getByXt(int locator) {
        return byExecutionToken.get(locator);
    }

    // memory access to the word's parameter area
    // is delegated to the word containing the address

    /**
     * Fetches an int value stored at a given address.
     *
     * @param address
     *                    locator of the target address
     * @return the value stored at the address
     */
    public Integer fetch(int address) {
        return findWordContainingPfa(address).fetch(address);
    }

    /**
     * Fetches a byte value stored at a given address.
     *
     * @param address
     *                    locator of the target address
     * @return the value stored at the address
     */
    public int cFetch(int address) {
        return findWordContainingPfa(address).cFetch(address);
    }

    /**
     * Stores an int value at a given address.
     *
     * Since the value is usually saved in an Integer array, we call the method with
     * an Integer object. We do some math with the address, so we use an int value
     * here.
     *
     * @param address
     *                    locator of the target address
     * @param value
     *                    value to store at the address
     */
    public void store(int address, Integer value) {
        findWordContainingPfa(address).store(address, value);
    }

    /**
     * Stores a byte value at a given address.
     *
     * @param address
     *                    locator of the target address
     * @param value
     *                    value whose lower byte will be stored at the address
     */
    public void cStore(int address, int value) {
        findWordContainingPfa(address).cStore(address, value & 0xFF);
    }

    /**
     * Allocates a byte in the current word and stores the value.
     *
     * @param value
     *                  value to add
     */
    public void addByte(int value) {
        allot(1);
        cStore(toLocator(currentWord.xt, currentWord.cellCount(), bytesAllocated), value);
    }

    /**
     * Gets the word whose address ist stored in the given pfa-Address.
     *
     * Works only for cell aligned word definitions.
     *
     * @param pfa
     *                locator containing the word's xt
     * @return the word found
     */
    public Word fetchWord(int pfa) {
        Word word = findWordContainingPfa(pfa);
        if (word instanceof CellListWord) {
            return byExecutionToken.get(word.fetch(pfa));
        }
        // FIXME: This is actually an illegal access...
        return word;
    }

    /**
     * Finds the word that contains the pfa address.
     *
     * @param pfa
     *                address contained by the wanted word
     * @return the word found
     */
    public Word findWordContainingPfa(int pfa) {
        return byExecutionToken.get(MemoryMapper.toXt(pfa));
    }

    /**
     * Gets the next available aligned dictionary address as locator.
     *
     * @return next available aligned dictionary address as locator
     */
    public Integer getHereValue() {
        return toLocator(currentWord.xt, currentWord.cellCount() + 1, 0);
    }

    /**
     * Forgets all words from the given word upwards.
     *
     * @param fenceWord
     *                      the first word to forget
     */
    public void forget(Word fenceWord) {
        int border = Math.max(MemoryMapper.following(fence), fenceWord.xt);
        reverse(memory) //
                .filter(word -> word.xt >= border) //
                .forEach(this::forgetWord);

        memory.removeIf(w -> w.xt >= border);
        removeTokenFromList(border);
    }

    private void removeTokenFromList(int border) {
        Iterator<Entry<Integer, Word>> iterator = byExecutionToken.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, Word> entry = iterator.next();
            if (entry.getKey() > border) {
                iterator.remove();
            }
        }
    }

    /**
     * Remove a word definition.
     *
     * @param word
     *                 first word to forget
     */
    public void forgetWord(Word word) {
        searchResolver.getVocabulary(word.vocabulary).forget(word);
        if (word instanceof VocabularyWord) {
            searchResolver.forgetVocabulary((VocabularyWord) word);
        }
        byExecutionToken.remove(word.xt);
    }

    /**
     * Allots space for n bytes in the parameter area of the current word.
     *
     * @param n
     *              number of bytes to allocate
     */
    public void allot(int n) {
        if (currentWord instanceof CellListWord) {
            int needed = cellsToAllot(n);
            for (int i = 0; i < needed; i++) {
                currentWord.addPfaEntry(0);
            }
        }
        // FIXME: Allocation is dictionary business. The word itself shouldn't be
        // bothered
        if (currentWord instanceof StringWord) {
            StringWord string = (StringWord) currentWord;
            string.allot(n);
        }
        bytesAllocated = (bytesAllocated + n) % CELL_SIZE;
    }

    /**
     * calculates the number of cells to allot from the number of bytes to allot.
     *
     * @param bytesToAllot
     *                         number of bytes to allot
     * @return number of cells to allot
     */
    private int cellsToAllot(int bytesToAllot) {
        return (bytesToAllot - (bytesAllocated > 0 ? CELL_SIZE - bytesAllocated : 0)) / CELL_SIZE;
    }

    /**
     * Aligns the memory area to the cell size.
     *
     * The complete cell is already allocated, just set the pointer to the last byte
     * in the cell
     */
    public void align() {
        bytesAllocated = 0;
    }

    /**
     * Access to the defining words in the directory ordered by creation time.
     *
     * @return {@link List} containing the words
     */
    public List<Word> memory() {
        return memory;
    }

    /**
     * memory location of the last word not to forget.
     *
     * @return address of the last word not to forget
     */
    public int getFence() {
        return fence;
    }

    /**
     * F83: return an access word for the fence varaiable.
     *
     * @param name
     *                 name of the FENCE word -- usually "FENCE"
     * @return a Word for the variable access
     */
    public Word fenceWord(String name) {
        return new UserVariableWord("FENCE", () -> fence, f -> fence = f);
    }
}
