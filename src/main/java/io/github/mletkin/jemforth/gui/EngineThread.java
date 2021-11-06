package io.github.mletkin.jemforth.gui;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.github.mletkin.jemforth.Package;
import io.github.mletkin.jemforth.engine.Callback;
import io.github.mletkin.jemforth.engine.Inspectable;
import io.github.mletkin.jemforth.engine.exception.JemForthException;

/**
 * Run a forth command in a separate thread.
 *
 * Thread execution is controlled through the {@code suspended} condition.
 */
@Package(cause = "used only by ThreadControl")
final class EngineThread extends Thread {

    private final Lock lock = new ReentrantLock();
    private final Condition suspended = lock.newCondition();

    private final Inspectable engine;
    private final String command;
    private final Callback debugCallback;
    private final EngineThreadCallback onTermination;

    @Package(cause = "used only by ThreadControl")
    @FunctionalInterface
    interface EngineThreadCallback {
        void call();
    }

    /**
     * create a thread objekt for the command execution.
     *
     * @param engine
     *                          engine that shall run the command
     * @param command
     *                          sommand string to execute
     * @param debugCallback
     *                          to be called in the interpreter loop
     * @param onTermination
     *                          to be called on termination
     */
    public EngineThread(Inspectable engine, String command, Callback debugCallback,
            EngineThreadCallback onTermination) {
        this.engine = engine;
        this.command = command;
        this.debugCallback = debugCallback;
        this.onTermination = onTermination;
    }

    @Override
    public void run() {
        engine.print("> " + command + "\n");
        try {
            engine.setDebugCallback(debugCallback);
            engine.process(command);
            engine.print(" ok.\n");
        } catch (JemForthException e) {
            engine.print(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            engine.print("\nException " + e.getClass().getSimpleName() + "\n");
        } finally {
            onTermination.call();
        }
    }

    public void pauseExecution() {
        lock.lock();
        try {
            suspended.await();
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted Execution");
        } finally {
            lock.unlock();
        }
    }

    public void continueExecution() {
        lock.lock();
        try {
            suspended.signal();
        } finally {
            lock.unlock();
        }
    }

}
