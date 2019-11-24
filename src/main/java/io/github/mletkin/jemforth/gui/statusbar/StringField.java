package io.github.mletkin.jemforth.gui.statusbar;

import io.github.mletkin.jemforth.engine.StringWord;
import io.github.mletkin.jemforth.engine.Word;

/**
 * Wraps a {@link StringWord}-Object for display.
 */
public class StringField extends DataField {

    private StringWord word;

    public StringField(Word word) {
        this.word = (StringWord) word;
        label.setText(word.name());
        field.setEditable(false);
        refresh();
    }

    @Override
    protected String getData() {
        return word.data();
    }

}
