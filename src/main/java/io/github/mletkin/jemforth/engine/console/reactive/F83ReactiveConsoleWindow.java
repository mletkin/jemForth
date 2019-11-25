package io.github.mletkin.jemforth.engine.console.reactive;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;

import io.github.mletkin.jemforth.engine.JemEngine;
import io.github.mletkin.jemforth.engine.UserVariableWord;
import io.github.mletkin.jemforth.engine.console.BarCaret;
import io.github.mletkin.jemforth.engine.exception.ForthTerminatedException;
import io.github.mletkin.jemforth.engine.exception.JemForthException;
import io.github.mletkin.jemforth.engine.f83.ReactiveEnvironment;
import io.github.mletkin.jemforth.gui.General;

/**
 * Simple console emulation as forth environment.
 * <ul>
 * <li>A {@link JConsole} text area acts as keyboard and display.
 * <li>The Forth engine processes input until "BYE" is executed.
 * <li>All control characters are passed to the engine.
 * <li>The console color and font size can be changed via forth words.
 * </ul>
 */
public class F83ReactiveConsoleWindow {

    private static final int AMBER = 0xFFB000;

    /**
     * List of the fonts supported by the console.
     */
    Font[] fonts = { //
            mkFont("consolas"), //
            mkFont("Monospaced"), //
            mkFont("pcsenior"), //
            mkFont("apple_2_40"), //
            mkFont("apple_2_80"), //
            mkFont("c_64_40"), //
            mkFont("c_64_80"), //
            mkFont("cbm_pet") //
    };

    private JFrame frame = new JFrame("Forth 83 Reactive Console");
    private JConsole console = new JConsole(frame);
    private int size;
    private int color;
    private int font;
    private ReactiveEnvironment env;

    /**
     * Creates the console window and run the forth interpreter in a new thread.
     *
     * @param engine
     *            forth engine to use
     */
    public F83ReactiveConsoleWindow(JemEngine engine) {
        env = new ReactiveEnvironment(engine);

        setupFrame();
        setupConsole();
        extendEngine();

        console.connect(env);

        new Thread(() -> this.runForth()).start();
    }

    /**
     * Initializes the main frame window.
     */
    private void setupFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.getContentPane().add(General.mkVerticalScrollPane(console), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Initializes the console with default values.
     */
    private void setupConsole() {
        console.setBackground(Color.BLACK);
        console.setCaret(new BarCaret());
        setColor(AMBER);
        setFont(0, 20);
    }

    /**
     * Runs the engine.
     * <ul>
     * <li>engine exceptions restart with a message
     * <li>engine reset reenters the engine retaining the stack
     * <li>termination (BYE) stops the engine
     * </ul>
     */
    private void runForth() {
        String cmd = ".( jemFORTH-83 V 1.0 ) ABORT";
        while (cmd != null) {
            try {
                env.engine().process(cmd);
                cmd = "QUIT";
            } catch (ForthTerminatedException e) {
                env.print("Execution stopped");
                cmd = null;
            } catch (JemForthException e) {
                cmd = ".( " + e.getMessage() + ") QUIT";
            }
        }
    }

    /**
     * Sets type and size of the console font.
     *
     * @param font
     *            number of the font in the list
     * @param size
     *            font size
     */
    private void setFont(int font, int size) {
        this.font = (font > 0 && font < fonts.length) ? font : 0;
        this.size = size;
        console.setFont(fonts[this.font].deriveFont(this.size * 1.0F));
    }

    /**
     * Sets the color of the console font.
     *
     * @param color
     *            color as 24 bit rgb-value
     */
    private void setColor(int color) {
        this.color = color;
        console.setColor(new Color(color));
    }

    private String fontList() {
        StringBuffer list = new StringBuffer("\n");
        for (int n = 0; n < fonts.length; n++) {
            list.append(n).append(": ").append(fonts[n].getFontName()).append("\n");
        }
        return list.toString();
    }

    private Font mkFont(String name) {
        try {
            InputStream file = getClass().getResourceAsStream("/fonts/" + name + ".ttf");
            return Font.createFont(Font.TRUETYPE_FONT, file);
        } catch (FontFormatException | IOException e) {
            System.out.println("unable to read font " + name);
        }
        return new Font(name, Font.BOLD, size);
    }

    /**
     * Add console specific commands to engine.
     */
    private void extendEngine() {
        env.engine().process("VOCABULARY CRT");
        env.engine().process("CRT DEFINITIONS");
        env.engine().add(new UserVariableWord("size", () -> this.size, v -> this.setFont(this.font, v)));
        env.engine().add(new UserVariableWord("color", () -> this.color, v -> this.setColor(v)));
        env.engine().add(new UserVariableWord("font", () -> this.font, v -> this.setFont(v, this.size)));
        env.engine().add("fontlist", e -> env.print(fontList()));

        env.engine().process("$FFB000 CONSTANT AMBER");
        env.engine().process("$33FF33 CONSTANT GREEN");
        env.engine().process("FORTH DEFINITIONS");
    }

}
