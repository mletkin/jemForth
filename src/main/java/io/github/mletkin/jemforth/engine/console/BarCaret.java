package io.github.mletkin.jemforth.engine.console;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

/**
 * Bar shaped caret for retro style console.
 */
public class BarCaret extends DefaultCaret {

    private static final int CARET_BLINK_RATE = 500;
    private static final Color CARET_COLOR = Color.WHITE;
    private static final String CARET_SHAPE = "_";

    /**
     * Creates a blinking caret.
     */
    public BarCaret() {
        setBlinkRate(CARET_BLINK_RATE);
    }

    @Override
    protected synchronized void damage(Rectangle r) {
        if (r != null) {
            adjustToCurrentFont(r);
        }
    }

    private void adjustToCurrentFont(Rectangle r) {
        FontMetrics fm = getComponent().getFontMetrics(getComponent().getFont());
        x = r.x;
        y = r.y;
        width = fm.stringWidth(CARET_SHAPE);
        height = fm.getHeight();
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        Rectangle r = componentRectangle();
        if (r == null) {
            return;
        }

        if ((x != r.x) || (y != r.y)) {
            repaint(); // erase previous location of caret
            damage(r);
        }

        if (isVisible()) {
            FontMetrics fm = getComponent().getFontMetrics(getComponent().getFont());
            g.setColor(CARET_COLOR);
            g.drawString(CARET_SHAPE, x, y + fm.getAscent());
        }
    }

    Rectangle componentRectangle() {
        try {
            if (getComponent() != null) {
                return getComponent().modelToView(getDot());
            }
        } catch (BadLocationException e) {
            // siently ignore exception
        }
        return null;
    }

}
