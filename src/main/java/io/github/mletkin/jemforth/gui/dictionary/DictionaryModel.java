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
        switch (columnIndex) {
        case 0:
            return "Adr";
        case 1:
            return "I";
        case 2:
            return "Name";
        case 3:
            return "Type";
        case 4:
            return "Voc";
        default:
            return null;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Word word = list.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return word.xt();
        case 1:
            return word.isImmediate() ? "x" : "";
        case 2:
            return word;
        case 3:
            return Inspector.CodeType.find(word).shortName();
        case 4:
            return word.vocabulary;
        default:
            return null;
        }
    }

}
