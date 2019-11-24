package io.github.mletkin.jemforth.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import io.github.mletkin.jemforth.engine.Util;

/**
 * R/O JTextArea attached to a PipedOutputStream.
 */
public class PipedConsole extends JTextArea implements Settable {

    protected JFrame frame;
    // signals the thread that it should exit
    private boolean quit = false;
    private Thread inputReader;
    private PipedInputStream inputPipe = new PipedInputStream();

    /**
     * Create a Console Component attached to the given {@link JFrame}
     *
     * @param frame
     *            frame containing the console.
     */
    public PipedConsole(JFrame frame) {
        super(25, 20);
        this.frame = frame;

        this.setEditable(false);
        this.setLineWrap(true);
        // ((DefaultCaret) this.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        frame.addWindowListener(getWindowListener());

        // start reading from incoming stream to console
        inputReader = new Thread(this::run);
        inputReader.setDaemon(true);
        inputReader.start();
    }

    public PipedConsole(JFrame frame, PipedOutputStream outStream) {
        this(frame);
        connect(outStream);
    }

    /**
     * Connect an outStream to the inputPipe for display.
     *
     * @param outStream
     *            Stream to use as display
     */
    public void connect(PipedOutputStream outStream) {
        try {
            outStream.connect(inputPipe);
        } catch (java.io.IOException | SecurityException e) {
            append("Couldn't redirect outputStream to the console\n" + e.getMessage());
        }
    }

    /**
     * copy incoming data from the inputPipe to the display.
     */
    public synchronized void run() {
        try {
            while (Thread.currentThread() == inputReader || quit) {
                try {
                    this.wait(100);
                } catch (InterruptedException e) {}
                if (inputPipe.available() > 0) {
                    append(Util.readLine(inputPipe, () -> quit));
                }
            }
        } catch (Exception e) {
            append("Exception while reading from stream: " + e);
        }
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
                quit = true;
                this.notifyAll(); // stop all threads
                try {
                    inputReader.join(1000);
                    Util.closeSilently(inputPipe);
                } catch (Exception e) {}
                System.exit(0);
            }

            @Override
            public synchronized void windowClosing(WindowEvent evt) {
                frame.setVisible(false);
                frame.dispose();
            }
        };
    }
}
