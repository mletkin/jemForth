package io.github.mletkin.jemforth.gui;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Menu bar for the Forth Gui.
 * <ul>
 * <li>Defines the menu structure
 * <li>Actions are executed by the main Window.
 * </ul>
 */
public class ForthGuiMenu extends JMenuBar {

    public ForthGuiMenu(ForthGui gui) {
        add(mkFileMenu(gui));
        add(mkOptionMenu(gui));
    }

    private JMenu mkFileMenu(ForthGui gui) {
        JMenu menu = new JMenu("File");
        menu.add(mkItem("Load file...", gui.loadAction));
        menu.add(mkItem("Run file...", gui.runAction));
        menu.add(mkItem("Save file...", gui.saveAction));
        menu.add(mkItem("Exit", e -> gui.close()));
        return menu;
    }

    private JMenu mkOptionMenu(ForthGui gui) {
        JMenu menu = new JMenu("Font");
        menu.add(mkItem("Small", e -> gui.setFontSize(10)));
        menu.add(mkItem("Normal", e -> gui.setFontSize(12)));
        menu.add(mkItem("Large", e -> gui.setFontSize(20)));
        return menu;
    }

    private JMenuItem mkItem(String title, ActionListener action) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(action);
        return menuItem;
    }
}
