/**
 * The JemForth project
 *
 * (C) 2017 by the Big Shedder
 */
package io.github.mletkin.jemforth.engine.f83;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.github.mletkin.jemforth.engine.exception.MassStorageException;
import io.github.mletkin.jemforth.engine.exception.MassStorygeCapacityExceededException;

/**
 * The Block processing emulation of a typical Forth 83 system.
 */
public class BlockBuffer {

    public static final int BLOCK_SIZE = 1024;
    private static final int MAX_BLOCK = 1024;
    private static final int MAX_BUFFER = 16;

    private int current;

    private int[] block = new int[MAX_BUFFER];
    private byte[][] content = new byte[MAX_BUFFER][];
    private boolean[] updated = new boolean[MAX_BUFFER];

    private RandomAccessFile file = null;

    /**
     * Creates a Block Buffer.
     * <p>
     * <ul>
     * <li>All buffers are set to "empty"
     * <li>A file may be used as "mass storage"
     * <ul>
     */
    public BlockBuffer(String path) {
        for (int n = 0; n < MAX_BUFFER; n++) {
            block[n] = 0;
            updated[n] = false;
        }
        use(path);
    }

    /**
     * Use the given file as mass storage file
     *
     * @param path
     *            path and filename of mass storage file
     */
    private void use(String path) {
        try {
            File newFile = new File(path);
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            file = new RandomAccessFile(newFile, "rw");
        } catch (IOException e) {
            System.out.println("Could not create file [" + path + "] " + e.getMessage());
        }
    }

    /**
     * 7.6.1.0800 BLOCK ( u -- a-addr )
     *
     * a-addr is the address of the first character of the block buffer assigned to
     * mass-storage block u. An ambiguous condition exists if u is not an available
     * block number.
     *
     * @param blockId
     *            ID of the block to use
     * @return id of assigned buffer
     */
    public int block(int blockId) {
        current = assignBuffer(blockId);
        if (block[current] != blockId) {
            block[current] = blockId;
            content[current] = loadBlock(blockId);
        }
        return current;
    }

    public byte[] blockContent(int blockId) {
        return content[block(blockId)];
    }

    /**
     * 7.6.1.0820 BUFFER ( u -- a-addr )
     *
     * a-addr is the address of the first character of the block buffer assigned to
     * block u. The contents of the block are unspecified. An ambiguous condition
     * exists if u is not an available block number. Assigns the block to the next
     * free buffer.
     *
     * TODO selection algorithm might need refinement.
     *
     * @param blockId
     *            ID of the block to use
     * @return id of assigned buffer
     */
    public int assignBuffer(int blockId) {
        for (int n = 0; n < MAX_BUFFER; n++) {
            if (block[n] == blockId) {
                return n;
            }
        }
        // find first empty buffer
        for (int n = 0; n < MAX_BUFFER; n++) {
            if (block[n] == 0) {
                return n;
            }
        }
        // no empty buffer, find first updated block
        for (int n = 0; n < MAX_BUFFER; n++) {
            if (updated[n]) {
                storeBuffer(n);
                return n;
            }
        }
        // no empty buffer, no updated => take the first one
        return 0;
    }

    /**
     * 7.6.1.2180 SAVE-BUFFERS ( -- )
     *
     * Transfer the contents of each UPDATEd block buffer to mass storage.<br>
     * Mark all buffers as unmodified.
     */
    public void saveUpdated() {
        for (int n = 0; n < MAX_BUFFER; n++) {
            if (updated[n]) {
                storeBuffer(n);
                updated[n] = false;
            }
        }
    }

    /**
     * 7.6.1.1559 FLUSH ( -- )
     *
     * Perform the function of SAVE-BUFFERS, then unassign all block buffers.
     */
    public void flushBuffers() {
        saveUpdated();
        emptyBuffers();
    }

    /**
     * 7.6.2.1330 EMPTY-BUFFERS ( -- )
     *
     * Unassign all block buffers.<br>
     * Do not transfer the contents of any UPDATEd block buffer to mass storage.
     */
    public void emptyBuffers() {
        for (int n = 0; n < MAX_BUFFER; n++) {
            block[n] = 0;
            updated[n] = false;
            content[n] = null;
        }
    }

    /**
     * load a block from the mass storage
     *
     * @param blockId
     *            id of block to load
     * @return byte array containing the block
     */
    private byte[] loadBlock(int blockId) {
        if (file == null) {
            throw new MassStorageException();
        }

        if (blockId > MAX_BLOCK) {
            throw new MassStorygeCapacityExceededException();
        }

        try {
            byte[] content = createBlock();
            if (blockId <= (file.length() + BLOCK_SIZE - 1) / BLOCK_SIZE) {
                file.seek((blockId - 1) * BLOCK_SIZE);
                file.read(content, 0, BLOCK_SIZE);
            }
            return content;
        } catch (IOException e) {
            throw new MassStorageException(e);
        }
    }

    private byte[] createBlock() {
        return new byte[BLOCK_SIZE];
    }

    /**
     * Write block content of buffer to mass storage.
     *
     * @param n
     *            number of Buffer to save buffer
     */
    private void storeBuffer(int n) {
        if (file == null) {
            throw new MassStorageException();
        }
        try {
            byte[] emptyBlock = createBlock();
            long numBlocks = ((block[n] - 1) * BLOCK_SIZE - file.length()) / BLOCK_SIZE;
            file.seek(file.length());
            for (int m = 1; m < numBlocks; m++) {
                file.write(emptyBlock);
            }
            file.seek((block[n] - 1) * BLOCK_SIZE);
            file.write(content[n]);
            updated[n] = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetch a byte from a block buffer.
     *
     * @param adr
     *            locator identifying the byte in the block buffer
     * @return fetched byte
     */
    public int cfetch(int adr) {
        return content[adr / BLOCK_SIZE][adr % BLOCK_SIZE];
    }

    /**
     * Store a byte in a block buffer.
     *
     * @param adr
     *            locator identifying the byte in the block buffer
     * @param value
     *            value to store
     */
    public void cStore(int adr, int value) {
        content[adr / BLOCK_SIZE][adr % BLOCK_SIZE] = (byte) (value & 0xFF);
    }

    /**
     * 7.6.1.2400 UPDATE ( -- )
     *
     * Mark the current block buffer as modified.<br>
     * UPDATE does not immediately cause I/O.
     */
    public void update() {
        updated[current] = true;
    }

    /**
     * Fetch character from Block blk at position toIn.
     *
     * the block is automatically fetched
     *
     * @param blk
     *            id of the block
     * @param toIn
     *            byte of the block to fetch
     * @return the byte at the identified position
     */
    public int cfetch(int blk, int toIn) {
        return blockContent(blk)[toIn];
    }
}
