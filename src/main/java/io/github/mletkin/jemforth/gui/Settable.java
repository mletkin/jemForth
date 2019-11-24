package io.github.mletkin.jemforth.gui;

import java.awt.Font;

/**
 * Interface for "settable" features in the GUI.
 *
 * Currently only the ability to change the font is implemented.
 */
public interface Settable {
    void setFont(Font font);
}
