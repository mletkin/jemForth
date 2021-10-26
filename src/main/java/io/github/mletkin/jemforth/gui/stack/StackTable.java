package io.github.mletkin.jemforth.gui.stack;

import java.util.List;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import io.github.mletkin.jemforth.gui.Refreshable;

/**
 * Table to display the contents of a stack.
 */
public class StackTable extends JTable implements Refreshable {

    private final TableCellRenderer numberRenderer = mkNumberRenderer();
    private Function<Integer, String> formatter = num -> Integer.toString(num);

    /**
     * Create a tbale for the given stack.
     *
     * @param stack
     *                  stack to display
     */
    public StackTable(List<Integer> stack) {
        super(new StackModel(stack));
        getColumnModel().getColumn(0).setWidth(20);
    }

    /**
     * Set the formatter for the data cells.
     *
     * @param formatter
     *                      number formatter to use
     * @return the instance
     */
    public StackTable with(Function<Integer, String> formatter) {
        this.formatter = formatter != null ? formatter : this.formatter;
        return this;
    }

    /**
     * Set the title of the single column.
     *
     * @param title
     *                  column title to use
     * @return the instance
     */
    public StackTable withTitle(String title) {
        this.columnModel.getColumn(0).setHeaderValue(title);
        return this;
    }

    @Override
    public void refresh() {
        this.resizeAndRepaint();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return numberRenderer;
    }

    /**
     * Format number right aligned with the configured formatter.
     *
     * @return {@link TableCellRenderer} instance
     */
    private TableCellRenderer mkNumberRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Integer) {
                    super.setValue(formatter.apply((Integer) value));
                } else {
                    super.setValue(value != null ? value.toString() : "null");
                }
            };
        };
        renderer.setHorizontalAlignment(JLabel.RIGHT);
        return renderer;
    }
}
