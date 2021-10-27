package io.github.mletkin.jemforth.engine.console.reactive;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import io.github.mletkin.jemforth.engine.f83.ReactiveEnvironment;

/**
 * Text Area, that emulates an I/O-console for a FORTH {@code JemEngine}.
 * <p>
 * Emulates a monochrome display with choosable color and font.
 */
public final class JConsole extends JTextArea {

    private static final Character BACKSPACE = Character.valueOf('\b');
    private static final Character FORMFEED = Character.valueOf((char) 0x0C);

    private final SubmissionPublisher<Character> publisher = new SubmissionPublisher<>();
    private final Subscriber<Character> subscriber = new ConsoleSubscriber();

    /**
     * Subscriber that appends received characters to the console display.
     */
    private class ConsoleSubscriber implements Subscriber<Character> {

        private Subscription subscription;

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(Character zeichen) {
            append(zeichen);
            subscription.request(1);
        }

        @Override
        public void onComplete() {
            append(" -- Done");
        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Creates and initializes a console attached to a given frame.
     *
     * @param frame
     *                  The frame containing the console. The Frame is needed to
     *                  attach a WindowsListener.
     */
    public JConsole(JFrame frame) {
        setEditable(true);
        setColor(Color.GREEN);
        setLineWrap(true);
        startPublishing();
        frame.addWindowListener(getWindowListener());
    }

    /**
     * Appends a single character to the display area.
     *
     * @param character
     *                      the character to process
     */
    public synchronized void append(Character character) {
        if (BACKSPACE.equals(character)) {
            replaceRange("", getDocument().getLength() - 1, getDocument().getLength());
        } else if (FORMFEED.equals(character)) {
            clear();
        } else if (character != null) {
            setEnabled(false);
            super.append(character.toString());
            setCaretPosition(getText().length());
            setEnabled(true);
        }
    }

    /**
     * Clears the display area.
     */
    private synchronized void clear() {
        setText("");
    }

    /**
     * Connects a reactive engine environmnt to the {@link JConsole}.
     *
     * @param env
     *                engine to connect
     */
    public void connect(ReactiveEnvironment env) {
        env.connectKeyboard(publisher);
        env.connectDisplay(subscriber);
    }

    /**
     * Installs a {@link KeyListener} that publishes the keys entered.
     * <p>
     * Suppresses repease and press events for enter and back space keys.
     *
     * @return KeyListener instance to use
     */
    private void startPublishing() {
        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (!publisher.isClosed()) {
                    publisher.submit(e.getKeyChar());
                }
                e.consume();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                suppressEvents(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                suppressEvents(e);
            }

            private void suppressEvents(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }

        });
    }

    /**
     * Cleans up, when the window is closed.
     *
     * @return WindowListener performing the task
     */
    private WindowListener getWindowListener() {
        return new WindowAdapter() {
            @Override
            public synchronized void windowClosing(WindowEvent evt) {
                this.notifyAll(); // stop all threads
                publisher.close();
            }
        };
    }

    /**
     * Sets the text color.
     * <p>
     * Enabled and disabled text have the same color.
     *
     * @param color
     *                  color to set
     */
    public void setColor(Color color) {
        setForeground(color);
        setDisabledTextColor(color);
    }
}
