package io.github.mletkin.jemforth.gui;

import io.github.mletkin.jemforth.engine.Callback;
import io.github.mletkin.jemforth.engine.Inspectable;
import io.github.mletkin.jemforth.engine.Util;
import io.github.mletkin.jemforth.gui.ForthGui.State;

/**
 * Controls the thread for the engine execution.
 */
public class ThreadControl {

    private final Callback WAIT = e -> this.suspendExecution();
    private final Callback RUN = e -> {};
    private final Callback STOP = e -> e.reset(true);

    private ForthGui forthGui;
    private EngineThread thread;
    private Inspectable engine;

    public ThreadControl(ForthGui forthGui, Inspectable engine) {
        this.forthGui = forthGui;
        this.engine = engine;
    }

    public void startExecution(String command) {
        if (threadAlive()) {
            resumeExecution(RUN);
            return;
        }
        if (!Util.isEmpty(command)) {
            startThread(command, RUN);
        }
    }

    public void stepExecution(String command) {
        if (!threadAlive()) {
            startThread(command, WAIT);
            return;
        }
        resumeExecution(WAIT);
    }

    public void stopExecution() {
        resumeExecution(STOP);
    }

    public void stepOut() {
        int depth = engine.getReturnStack().depth();
        resumeExecution(e -> {
            if (e.getReturnStack().depth() < depth)
                WAIT.call(e);
        });
    }

    public void stepOver() {
        int depth = engine.getReturnStack().depth();
        resumeExecution(e -> {
            if (e.getReturnStack().depth() <= depth)
                WAIT.call(e);
        });
    }

    public void pauseExecution() {
        if (forthGui.state == State.RUNNING) {
            engine.setDebugCallback(WAIT);
        }
    }

    private void startThread(String command, Callback callback) {
        forthGui.setState(State.RUNNING);
        thread = new EngineThread(engine, command, callback, () -> forthGui.setState(ForthGui.State.HALTED));
        thread.start();
    }

    /**
     * suspend the current execution.
     */
    private void suspendExecution() {
        forthGui.setState(ForthGui.State.PAUSING);
        thread.pauseExecution();
    }

    /**
     * Resume the execution of the currently executed statement.
     */
    private void resumeExecution(Callback callback) {
        forthGui.setState(ForthGui.State.RUNNING);
        engine.setDebugCallback(callback);
        thread.continueExecution();
    }

    private boolean threadAlive() {
        return thread != null && thread.isAlive();
    }

    public void kill() {
        if (threadAlive()) {
            thread.stop();
        }
    }
}
