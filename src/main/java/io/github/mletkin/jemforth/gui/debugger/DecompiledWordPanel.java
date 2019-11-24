package io.github.mletkin.jemforth.gui.debugger;

import java.util.List;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import io.github.mletkin.jemforth.engine.Inspectable;
import io.github.mletkin.jemforth.engine.MemoryMapper;
import io.github.mletkin.jemforth.engine.Util;
import io.github.mletkin.jemforth.engine.Word;
import io.github.mletkin.jemforth.gui.Refreshable;

/**
 * Text area to display a word as decompiled list
 */
public class DecompiledWordPanel extends JTextArea implements Refreshable {

    private static final String CR = "\n";
    private static final String POINTER = "> ";

    private Inspectable engine;

    /**
     * Create a panel connected to an engine.
     *
     * @param engine
     *            engine to use
     */
    public DecompiledWordPanel(Inspectable engine) {
        super(1, 20);
        setEditable(false);
        this.engine = engine;
        refresh();
    }

    private String getContent() {
        Word word = engine.getDictionary().findWordContainingPfa(engine.getIp());
        if (word == null) {
            return "";
        }
        if (!word.getDataArea().findAny().isPresent()) {
            return word.name();
        }
        return decompileWord(word);
    }

    private String decompileWord(Word word) {
        StringBuffer result = new StringBuffer();
        List<String> list = engine.getInspector().decompileWordList(word);
        int currentPosition = MemoryMapper.toCellPosition(engine.getIp());
        String sep = "";
        for (int n = 0; n < list.size(); n++) {
            result.append(sep).append(n + 1 == currentPosition ? POINTER : "  ").append(list.get(n));
            sep = CR;
        }
        return result.toString();
    }

    private void scrollToCurrentCodeLine() {
        try {
            int pos = getPositionOfCurrentCodeLine();
            if (pos > 0) {
                scrollRectToVisible(modelToView(pos));
                setCaretPosition(pos);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private int getPositionOfCurrentCodeLine() {
        String text = getText();
        if (Util.isEmpty(text)) {
            return 0;
        }
        return text.indexOf(POINTER, 0);
    }

    @Override
    public void refresh() {
        setText(getContent());
        scrollToCurrentCodeLine();
    }

}
