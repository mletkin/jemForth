package io.github.mletkin.jemforth.gui.dictionary;

import static io.github.mletkin.jemforth.engine.Util.doIfNotNull;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTable;

import io.github.mletkin.jemforth.engine.Inspectable;
import io.github.mletkin.jemforth.gui.BaseSelectPanel;
import io.github.mletkin.jemforth.gui.General;
import io.github.mletkin.jemforth.gui.Refreshable;
import io.github.mletkin.jemforth.gui.Settable;

/**
 * Panel to display the complete content of a Forth Dictionary.
 */
public class DictionaryPanel extends JPanel implements Settable, Refreshable {

    private final Inspectable engine;
    private JTable table;
    private BaseSelectPanel basePanel;

    /**
     * Create the panel and connect it to the engine.
     *
     * @param engine
     *                   engine to use
     */
    public DictionaryPanel(Inspectable engine) {
        super(new BorderLayout());
        this.engine = engine;

        this.add(mkButtonPanel(), BorderLayout.NORTH);
        this.add(mkDictionaryPanel(), BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(300, 400));

        refresh();
    }

    /**
     * Create a scrollable dictionary panel.
     *
     * @return
     */
    private Component mkDictionaryPanel() {
        table = new DictionaryTable(engine.getDictionary()).with(this::format);
        return General.mkVerticalScrollPane(table);
    }

    /**
     * Connect a number base selector to the engine and tut ist on a panel.
     *
     * @return
     */
    private JPanel mkButtonPanel() {
        JPanel btnPane = new JPanel();
        btnPane.setLayout(new FlowLayout(FlowLayout.LEFT));
        btnPane.add(basePanel = new BaseSelectPanel(engine::getBase, i -> this.refresh()));
        return btnPane;
    }

    @Override
    public void refresh() {
        doIfNotNull(table, JTable::repaint);
    }

    /**
     * Format numbers according to the base set in the base panel.
     *
     * @param wert
     * @return
     */
    private String format(Integer wert) {
        try {
            return Integer.toString(wert, this.basePanel.getBase()).toUpperCase();
        } catch (Exception e) {
            return "null";
        }
    }

    @Override
    public void setFont(Font font) {
        if (table != null) {
            table.setFont(font);
            table.setRowHeight(getFontMetrics(font).getHeight());
        }
    }

}
