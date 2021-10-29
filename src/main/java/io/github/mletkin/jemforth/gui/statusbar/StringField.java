package io.github.mletkin.jemforth.gui.statusbar;

import io.github.mletkin.jemforth.engine.StringWord;

/**
 * Wraps a {@link StringWord}-Object for display.
 */
public class StringField extends DataField {

    private final StringWord word;

    public StringField(StringWord word) {
        this.word = word;
        label.setText(word.name());
        field.setEditable(false);
        refresh();
    }

    @Override
    protected String getData() {
        return word.data();
    }

}
