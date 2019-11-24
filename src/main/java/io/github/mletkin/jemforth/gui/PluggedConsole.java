package io.github.mletkin.jemforth.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import io.github.mletkin.jemforth.engine.Inspectable;

/**
 * R/O JTextArea as display attached via method call to a JemEngine.
 */
public class PluggedConsole extends JTextArea implements Settable {

    protected JFrame frame;

    /**
     * Create a Console Component attached to the given {@link JFrame}
     *
     * @param frame
     *            frame containing the console.
     * @param engine
     *            the engine to connect
     */
    public PluggedConsole(JFrame frame, Inspectable engine) {
        super(25, 20);
        this.frame = frame;

        setEditable(false);
        setLineWrap(true);
        connect(engine);

        frame.addWindowListener(getWindowListener());
    }

    @Override
    public void append(String str) {
        if (str != null) {
            if (str.indexOf('\b') >= 0 || str.indexOf(0xC) >= 0) {
                str.chars().forEach(p -> append((char) p));
                return;
            }
            setEnabled(false);
            super.append(str);
            setCaretPosition(getText().length());
            setEnabled(true);
        }
    }

    /**
     * Display a single character.
     *
     * display specific actions for control characters are done here.
     *
     * @param str
     *            character to display
     */
    public synchronized void append(char str) {
        if (str == '\b') {
            replaceRange("", getDocument().getLength() - 1, getDocument().getLength());
        } else if (str == 0x0C) {
            clear();
        } else {
            setEnabled(false);
            super.append("" + str);
            setCaretPosition(getText().length());
            setEnabled(true);
        }
    }

    /**
     * clear the display
     */
    public synchronized void clear() {
        setText("");
    }

    WindowListener getWindowListener() {
        return new WindowAdapter() {

            @Override
            public synchronized void windowClosed(WindowEvent evt) {
                System.exit(0);
            }

            @Override
            public synchronized void windowClosing(WindowEvent evt) {
                frame.setVisible(false);
                frame.dispose();
            }
        };
    }

    private void connect(Inspectable engine) {
        engine.setCharPrinter(this::append);
        engine.setStringPrinter(this::append);
    }
}
