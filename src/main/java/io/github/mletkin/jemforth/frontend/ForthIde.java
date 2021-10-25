package io.github.mletkin.jemforth.frontend;

import java.io.IOException;

import io.github.mletkin.jemforth.engine.f83.Forth83Engine;
import io.github.mletkin.jemforth.gui.ForthGui;

/**
 * Create a {@link ForthGui} Frame and run the IDE.
 */
public final class ForthIde {

    private ForthIde() {
        // prevent instantiation
    }

    /**
     * Run the GUI with optional width and height as parameter.
     *
     * @param args
     *                 array with pogram arguments
     * @throws IOException
     *                         something went wrong...
     */
    public static void main(String[] args) throws IOException {
        new ForthGui(new Forth83Engine(), getValue(args, 0, 1024), getValue(args, 1, 768));
    }

    private static int getValue(String[] arg, int pos, int defvalue) {
        try {
            return pos < arg.length ? Integer.parseInt(arg[pos]) : defvalue;
        } catch (NumberFormatException e) {
            return defvalue;
        }
    }

}
