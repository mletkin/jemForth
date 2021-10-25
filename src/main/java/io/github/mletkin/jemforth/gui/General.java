package io.github.mletkin.jemforth.gui;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

/**
 * Convenience functions for the GUI.
 */
public final class General {

    // name of standard GUI font
    static final String FONT_NAME = "Consolas";

    // icon button size
    private static final Dimension BUTTON_DIM = new Dimension(20, 20);

    private General() {
        // prevent instantiation
    }

    public static JButton mkButton(String name, ActionListener action, ButtonIcon icon) {
        JButton button = new JButton(name);
        button.addActionListener(action);
        button.setIcon(icon.image());
        button.setToolTipText(icon != null ? icon.tip() : null);
        button.setPreferredSize(BUTTON_DIM);
        return button;
    }

    public static JButton mkButton(ActionListener action, ButtonIcon icon) {
        return mkButton("", action, icon);
    }

    public static JScrollPane mkVerticalScrollPane(JComponent component) {
        return new JScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public static JToggleButton mkToggleButton(ActionListener action, ButtonIcon icon) {
        JToggleButton button = new JToggleButton(icon.image());
        button.addActionListener(action);
        button.setToolTipText(icon != null ? icon.tip() : null);
        button.setPreferredSize(BUTTON_DIM);
        // for future use
        // button.setBorderPainted(false);
        // button.setFocusPainted(false);
        // button.setContentAreaFilled(false);
        return button;
    }

}
