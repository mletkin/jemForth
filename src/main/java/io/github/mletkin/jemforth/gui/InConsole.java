package io.github.mletkin.jemforth.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import io.github.mletkin.jemforth.engine.Util;

/**
 * A text area for entering console commands.
 * <ul>
 * <li>Ctrl-Enter esecutes the selection
 * <li>Ctrl-L extends the selection to whole lines
 * </ul>
 * TODO use key bindings
 */
public class InConsole extends JTextArea implements Settable {

    private static final String CR = "\n";
    private static final String PREFIX_COMMENT = "\\ ";

    private Consumer<String> executor;

    InConsole() {
        super(3, 20);
        setLineWrap(true);
        setTabSize(4);
        addKeyListener(mkKeyListener());
    }

    private KeyAdapter mkKeyListener() {
        return new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    execute();
                }
                if (e.getKeyCode() == KeyEvent.VK_E && e.isControlDown()) {
                    gotoLineEnd();
                }
                if (e.getKeyCode() == KeyEvent.VK_L && e.isControlDown() && !e.isShiftDown()) {
                    extendSelectionToWholeLines();
                }
                if (e.getKeyChar() == KeyEvent.VK_SLASH && e.isControlDown()) {
                    blockComment();
                }
                if (e.getKeyCode() == KeyEvent.VK_U && e.isControlDown() && e.isShiftDown()) {
                    processSelection(String::toUpperCase);
                }
                if (e.getKeyCode() == KeyEvent.VK_L && e.isControlDown() && e.isShiftDown()) {
                    processSelection(String::toLowerCase);
                }
            }
        };
    }

    /**
     * Set the executor to run the entered text.
     *
     * @param executor
     *            consumer to process the text.
     * @return the instance
     */
    public InConsole with(Consumer<String> executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Clear the console
     */
    public void clear() {
        this.setText("");
    }

    /**
     * Execute the selection
     */
    public void execute() {
        if (!Util.isEmpty(getSelection())) {
            executor.accept(getSelection());
        }
    }

    public String getSelection() {
        return getSelectedText();
    }

    public void extendSelectionToWholeLines() {
        try {
            setSelectionStart(getLineStartOffset(getLineOfOffset(getSelectionStart())));
            setSelectionEnd(getLineEndOffset(getLineOfOffset(getSelectionEnd())));
        } catch (BadLocationException e) {
            // do nothing
        }
    }

    public void appendLine(String s) {
        append(s);
        append(CR);
    }

    public void gotoLineEnd() {}

    /**
     * comment/uncomment the lines included in the selection
     */
    public void blockComment() {
        extendSelectionToWholeLines();
        processSelection(//
                getSelectedText().startsWith(PREFIX_COMMENT) ? this::removeComment : this::addComment);
    }

    /**
     * apply lambda to each line of selection
     *
     * @param process
     *            lambda to execute
     */
    private void processSelection(Function<String, String> process) {
        String selection = getSelectedText();
        replaceSelection(Arrays.stream(selection.split(CR)).map(process).collect(Collectors.joining(CR))
                + (selection.endsWith(CR) ? CR : ""));
    }

    private String removeComment(String s) {
        return s.startsWith(PREFIX_COMMENT) ? s.substring(2) : s;
    }

    private String addComment(String s) {
        return PREFIX_COMMENT + s;
    }

}
