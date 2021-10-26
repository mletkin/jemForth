package io.github.mletkin.jemforth.gui.debugger;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JTextArea;

import io.github.mletkin.jemforth.engine.Inspectable;
import io.github.mletkin.jemforth.engine.Word;
import io.github.mletkin.jemforth.gui.Refreshable;

/**
 * TextArea to display the decompiled call stack.
 */
public class CallStackPanel extends JTextArea implements Refreshable {

    private static final String NO_WORD_IN_EXECUTION = "no word in execution";
    private static final String LINE_PREFIX = "  ";

    private final Inspectable engine;

    /**
     * Create a panel connected to an engine.
     *
     * @param engine
     *                   engine to use
     */
    public CallStackPanel(Inspectable engine) {
        super(1, 20);
        setEditable(false);
        this.engine = engine;
        refresh();
    }

    /**
     * Convert return stack to word name list.
     *
     * @return the decompiled return stack
     */
    private List<String> decompileReturnStack() {
        return engine.getReturnStackContent() //
                .map(w -> engine.getDictionary().findWordContainingPfa(w)) //
                .filter(Objects::nonNull) //
                .map(Word::name) //
                .collect(Collectors.toList());
    }

    /*
     * Add Word referenced by the engine IP.
     *
     * @param list list to add the word
     *
     * @return the list with the word added
     */
    private List<String> addCurrentWord(List<String> list) {
        list.add(ofNullable(engine.getDictionary() //
                .findWordContainingPfa(engine.getIp())) //
                        .map(Word::name) //
                        .orElse(NO_WORD_IN_EXECUTION));
        return list;
    }

    private String getContent() {
        StringBuffer buffer = new StringBuffer();
        String prefix = LINE_PREFIX;
        String lineBreak = "";
        for (String line : addCurrentWord(decompileReturnStack())) {
            buffer.append(lineBreak).append(prefix).append(line);
            prefix += LINE_PREFIX;
            lineBreak = "\n";
        }
        return buffer.toString();
    }

    @Override
    public void refresh() {
        setText(getContent());
    }
}
