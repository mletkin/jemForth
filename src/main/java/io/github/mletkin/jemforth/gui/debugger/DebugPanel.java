package io.github.mletkin.jemforth.gui.debugger;

import static io.github.mletkin.jemforth.engine.Util.doIfNotNull;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;

import io.github.mletkin.jemforth.gui.ForthGui;
import io.github.mletkin.jemforth.gui.General;
import io.github.mletkin.jemforth.gui.Refreshable;
import io.github.mletkin.jemforth.gui.Settable;

/**
 * Combined panel for the debug view.
 * <ul>
 * <li>button panel for debug functions
 * <li>list view of the decompiled call stack
 * <li>list view of the decompiled current word
 * </ul>
 */
public class DebugPanel extends JPanel implements Settable, Refreshable {

    private DecompiledWordPanel wordPanel;
    private CallStackPanel callStackPanel;
    private DebugButtonPanel buttonPanel;

    public DebugPanel(ForthGui forthGui) {
        super(new BorderLayout());

        buttonPanel = new DebugButtonPanel(forthGui);
        callStackPanel = new CallStackPanel(forthGui.getEngine());
        wordPanel = new DecompiledWordPanel(forthGui.getEngine());

        this.add(buttonPanel, BorderLayout.NORTH);
        this.add(createCodePanel(), BorderLayout.CENTER);
    }

    /**
     * Create a combined panel for the code view.
     *
     * @return
     */
    private JPanel createCodePanel() {
        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.add(callStackPanel, BorderLayout.NORTH);
        codePanel.add(General.mkVerticalScrollPane(wordPanel), BorderLayout.CENTER);
        return codePanel;
    }

    @Override
    public void refresh() {
        doIfNotNull(buttonPanel, Refreshable::refresh);
        doIfNotNull(callStackPanel, Refreshable::refresh);
        doIfNotNull(wordPanel, Refreshable::refresh);
    }

    @Override
    public void setFont(Font font) {
        doIfNotNull(wordPanel, s -> s.setFont(font));
        doIfNotNull(callStackPanel, s -> s.setFont(font));
    }

}
