package io.github.mletkin.jemforth.gui.dictionary;

import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import io.github.mletkin.jemforth.engine.Dictionary;
import io.github.mletkin.jemforth.engine.Word;

/**
 * Table to display the words of a dictionary.
 */
public class DictionaryTable extends JTable {

    private final TableCellRenderer centerRenderer = mkAlignmentRenderer(JLabel.CENTER);
    private final TableCellRenderer numberRenderer = mkNumberRenderer();
    private final TableCellRenderer nameRenderer = new WordNameRenderer();

    private final Dictionary dict;
    private Function<Integer, String> formatter = num -> Integer.toString(num);

    public DictionaryTable(Dictionary dict) {
        super(new DictionaryModel(dict.memory()));
        this.dict = dict;
        setAutoCreateRowSorter(true);
        getColumnModel().getColumn(0).setWidth(80);
        getColumnModel().getColumn(1).setMaxWidth(20);
        getColumnModel().getColumn(2).setWidth(100);
        getColumnModel().getColumn(3).setWidth(50);
        getColumnModel().getColumn(4).setWidth(30);

        // TableRowSorter<TableModel> sorter = new
        // TableRowSorter<TableModel>(table.getModel());
        // table.setRowSorter(sorter);
        //
        // List<RowSorter.SortKey> sortKeys = new ArrayList<>(25);
        // sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        // sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        // sorter.setSortKeys(sortKeys);
    }

    public DictionaryTable with(Function<Integer, String> formatter) {
        this.formatter = formatter != null ? formatter : this.formatter;
        return this;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        switch (column) {
        case 0:
            return numberRenderer;
        case 1:
            return centerRenderer;
        case 2:
            return nameRenderer;
        default:
            return super.getCellRenderer(row, column);
        }
    }

    /**
     * formatter number right aligned with the configured formatter.
     *
     * @return
     */
    private TableCellRenderer mkNumberRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Integer) {
                    super.setValue(formatter.apply((Integer) value));
                }
            };
        };
        renderer.setHorizontalAlignment(JLabel.RIGHT);
        return renderer;
    }

    /**
     * Format String with the given alignment.
     *
     * @param alignment
     * @return
     */
    private TableCellRenderer mkAlignmentRenderer(int alignment) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(alignment);
        return renderer;
    }

    /**
     * Format name of a word.
     * <ul>
     * <li>If a word is not visible, strike it out.
     * <li>If a word is "fenced", make it bold
     * </ul>
     */
    class WordNameRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Word) {
                Word word = (Word) value;
                if (isHidden(word)) {
                    component.setFont(getStrikeOutFont(component.getFont()));
                }
                if (isFence(word)) {
                    component.setFont(getBoldFont(component.getFont()));
                }
            }
            return component;
        }

        @Override
        protected void setValue(Object value) {
            if (value instanceof Word) {
                super.setValue(((Word) value).name());
            }
        }

        private boolean isFence(Word word) {
            if (dict != null) {
                return word.xt.intValue() == dict.getFence();
            }
            return false;
        }

        private boolean isHidden(Word word) {
            if (dict != null) {
                return dict.find(word.name()) != word;
            }
            return false;
        }

        private Font getBoldFont(Font font) {
            return font.deriveFont(Font.BOLD);
        }

        private Font getStrikeOutFont(Font font) {
            Map fontAttributes = font.getAttributes();
            fontAttributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            return new Font(fontAttributes);
        }
    }

}
