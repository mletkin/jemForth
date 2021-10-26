package io.github.mletkin.jemforth.gui.debugger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import io.github.mletkin.jemforth.gui.ButtonIcon;
import io.github.mletkin.jemforth.gui.ForthGui;
import io.github.mletkin.jemforth.gui.General;
import io.github.mletkin.jemforth.gui.Refreshable;
import io.github.mletkin.jemforth.gui.ForthGui.State;

/**
 * Panel for debugging functions
 */
public class DebugButtonPanel extends JPanel implements Refreshable {

    private final JButton go;
    private final JButton pause;
    private final JButton step;
    private final JButton stepOver;
    private final JButton stepOut;
    private final JButton exit;
    private final JButton reset;
    private final ForthGui forthGui;

    public DebugButtonPanel(ForthGui forthGui) {
        super();
        this.forthGui = forthGui;
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(go = General.mkButton(e -> forthGui.go(), ButtonIcon.RESUME));
        add(pause = General.mkButton(e -> forthGui.pause(), ButtonIcon.SUSPEND));
        add(step = General.mkButton(e -> forthGui.stepInto(), ButtonIcon.STEP_INTO));
        add(stepOver = General.mkButton(e -> forthGui.stepOver(), ButtonIcon.STEP_OVER));
        add(stepOut = General.mkButton(e -> forthGui.stepOut(), ButtonIcon.STEP_OUT));
        add(exit = General.mkButton(e -> forthGui.stop(), ButtonIcon.STOP));
        add(reset = General.mkButton(e -> forthGui.reset(), ButtonIcon.RESET));
    }

    @Override
    public void refresh() {
        State state = forthGui.getState();
        step.setEnabled(state != State.RUNNING);
        stepOver.setEnabled(state != State.RUNNING);
        stepOut.setEnabled(state != State.RUNNING);
        exit.setEnabled(state != State.HALTED);
        go.setEnabled(state != State.RUNNING);
        pause.setEnabled(state == State.RUNNING);
        reset.setEnabled(true);
    }
}
