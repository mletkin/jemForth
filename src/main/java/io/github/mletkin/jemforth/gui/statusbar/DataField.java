package io.github.mletkin.jemforth.gui.statusbar;

import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * A label/field combination to display a data containing word.
 */
public abstract class DataField {

    JLabel label = new JLabel();
    JTextField field = new JTextField();

    public void refresh() {
        String str = asString(getData());
        field.setText(str);
        field.setColumns(str.length());
    }

    /**
     * get the data from the word.
     *
     * @return contained data
     */
    abstract protected Object getData();

    private String asString(Object what) {
        return what != null ? what.toString() : "null";
    }
}
