package io.github.mletkin.jemforth.gui.stack;

import static io.github.mletkin.jemforth.engine.Util.doIfNotNull;
import static io.github.mletkin.jemforth.gui.General.mkButton;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JPanel;

import io.github.mletkin.jemforth.gui.BaseSelectPanel;
import io.github.mletkin.jemforth.gui.ButtonIcon;
import io.github.mletkin.jemforth.gui.General;
import io.github.mletkin.jemforth.gui.Refreshable;
import io.github.mletkin.jemforth.gui.Settable;

/**
 * Create a panel connected to a Stack with a button panel on top.
 *
 * setting the font adjusts the panel width to savely show a 32 bit integer as
 * binary
 */
public class StackPanel extends JPanel implements Settable, Refreshable {

    private List<Integer> stack;
    private BaseSelectPanel basePanel;
    private StackTable table;

    /**
     * Create a panel displaying the stack of an engine and a button panel.
     *
     * @param stack
     *            the stack to display
     * @param title
     *            Title of the table column
     * @param getBase
     *            lamba to get the engine's number base
     */
    public StackPanel(List<Integer> stack, String title, Supplier<Integer> getBase) {
        super(new BorderLayout());

        this.stack = stack;
        add(mkButtonPanel(getBase), BorderLayout.NORTH);
        add(mkDataPanel(title), BorderLayout.CENTER);

        setSize(getPreferredWidth(), getHeight());
    }

    private Component mkDataPanel(String title) {
        table = new StackTable(stack).with(this::format).withTitle(title);
        return General.mkVerticalScrollPane(table);
    }

    /**
     * Clear the stack as well as the display.
     */
    private void clear() {
        stack.clear();
        refresh();
    }

    /**
     * Format the value of a single cell.
     *
     * @param wert
     * @return
     */
    private String format(Integer wert) {
        try {
            return Integer.toString(wert, basePanel.getBase()).toUpperCase();
        } catch (Exception e) {
            return "null";
        }
    }

    @Override
    public void refresh() {
        doIfNotNull(table, Refreshable::refresh);
    }

    /**
     * Create the button panel.
     *
     * @param getBase
     *
     * @return
     */
    private JPanel mkButtonPanel(Supplier<Integer> getBase) {
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPane.add(mkButton(e -> this.clear(), ButtonIcon.EMPTY));
        btnPane.add(basePanel = new BaseSelectPanel(getBase, i -> this.refresh()));
        return btnPane;
    }

    @Override
    public void setFont(Font font) {
        if (table != null) {
            table.setFont(font);
            setSize(getPreferredWidth(), getHeight());
            table.setRowHeight(getFontMetrics(font).getHeight());
        }
    }

    private int getPreferredWidth() {
        return 16 * getFontMetrics(table.getFont()).stringWidth("0");
    }
}
