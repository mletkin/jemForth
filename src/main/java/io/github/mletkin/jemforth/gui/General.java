package io.github.mletkin.jemforth.gui;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import io.github.mletkin.jemforth.Package;

/**
 * Convenience functions for the GUI.
 */
public final class General {

    /**
     * name of standard GUI font
     */
    @Package(cause = "used only by the main GUI class")
    static final String FONT_NAME = "Consolas";

    /**
     * icon button size
     */
    private static final Dimension BUTTON_DIM = new Dimension(20, 20);

    private General() {
        // prevent instantiation
    }

    /**
     * Creates a new button.
     * <p>
     * All parameters are mandatory
     *
     * @param name
     *                   name/text of the button
     * @param action
     *                   click action
     * @param icon
     *                   icon on the button
     * @return a new button object
     */
    public static JButton mkButton(String name, ActionListener action, ButtonIcon icon) {
        JButton button = new JButton(name);
        button.addActionListener(action);
        button.setIcon(icon.image());
        button.setToolTipText(icon.tip());
        button.setPreferredSize(BUTTON_DIM);
        return button;
    }

    /**
     * Creates a icon button wothout text.
     *
     * @param action
     *                   click action
     * @param icon
     *                   icon on the button
     * @return a new button object
     */
    public static JButton mkButton(ActionListener action, ButtonIcon icon) {
        return mkButton("", action, icon);
    }

    /**
     * Create a verticat scroll pane for acomponent.
     *
     * @param component
     *                      the component to scroll pane
     * @return the scroll pane containing the component
     */
    public static JScrollPane mkVerticalScrollPane(JComponent component) {
        return new JScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     * Create a button for a toggle action.
     *
     * @param action
     *                   click action
     * @param icon
     *                   icon on the button
     * @return a new button object
     */
    public static JToggleButton mkToggleButton(ActionListener action, ButtonIcon icon) {
        JToggleButton button = new JToggleButton(icon.image());
        button.addActionListener(action);
        button.setToolTipText(icon.tip());
        button.setPreferredSize(BUTTON_DIM);
        // for future use
        // button.setBorderPainted(false);
        // button.setFocusPainted(false);
        // button.setContentAreaFilled(false);
        return button;
    }

}
