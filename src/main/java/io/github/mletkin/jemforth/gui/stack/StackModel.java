package io.github.mletkin.jemforth.gui.stack;

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Model for the stack display table.
 * <ul>
 * <li>Top of stack is top of table
 * <li>An empty stack is visualized as text label
 * </ul>
 */
public class StackModel extends AbstractTableModel {

    private static final String STACK_EMPTY = "stack empty";
    private final List<Integer> list;

    public StackModel(List<Integer> content) {
        this.list = content;
    }

    @Override
    public int getRowCount() {
        return list.isEmpty() ? 1 : list.size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return "Value";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return list.isEmpty() ? STACK_EMPTY : list.get(list.size() - rowIndex - 1);
    }
}
