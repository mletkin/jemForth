package io.github.mletkin.jemforth.gui.dictionary;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import io.github.mletkin.jemforth.engine.Inspector;
import io.github.mletkin.jemforth.engine.words.Word;

/**
 * Model for the dictionary display table.
 */
public class DictionaryModel extends AbstractTableModel {

    private final List<Word> list;

    public DictionaryModel(List<Word> wordList) {
        this.list = wordList;
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return switch (columnIndex) {
        case 0 -> "Adr";
        case 1 -> "I";
        case 2 -> "Name";
        case 3 -> "Type";
        case 4 -> "Voc";
        default -> null;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Word word = list.get(rowIndex);
        return switch (columnIndex) {
        case 0 -> word.xt();
        case 1 -> word.isImmediate() ? "x" : "";
        case 2 -> word;
        case 3 -> Inspector.CodeType.find(word).shortName();
        case 4 -> word.vocabulary;
        default -> null;
        };
    }

}
