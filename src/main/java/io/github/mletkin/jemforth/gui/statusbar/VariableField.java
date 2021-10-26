package io.github.mletkin.jemforth.gui.statusbar;

import io.github.mletkin.jemforth.engine.VariableWord;
import io.github.mletkin.jemforth.engine.Word;

/**
 * Wraps a {@link VariableWord}-Object for display.
 */
public class VariableField extends DataField {

    private final Word word;

    public VariableField(Word word) {
        this.word = word;
        label.setText(word.name());
        field.setEditable(false);
        refresh();
    }

    @Override
    protected Integer getData() {
        return word.fetch(word.xt);
    }
}
