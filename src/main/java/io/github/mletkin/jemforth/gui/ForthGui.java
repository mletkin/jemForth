package io.github.mletkin.jemforth.gui;

import static io.github.mletkin.jemforth.engine.Util.doIfNotNull;
import static io.github.mletkin.jemforth.gui.General.mkVerticalScrollPane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import io.github.mletkin.jemforth.engine.Inspectable;
import io.github.mletkin.jemforth.engine.UserVariableWord;
import io.github.mletkin.jemforth.engine.Util;
import io.github.mletkin.jemforth.gui.debugger.DebugPanel;
import io.github.mletkin.jemforth.gui.dictionary.DictionaryPanel;
import io.github.mletkin.jemforth.gui.settings.Access;
import io.github.mletkin.jemforth.gui.settings.Props;
import io.github.mletkin.jemforth.gui.stack.StackPanel;
import io.github.mletkin.jemforth.gui.statusbar.StatusBar;

/**
 * Main-Frame of the Forth-"Eclipse"
 */
public class ForthGui implements Refreshable {

    public enum State {
        RUNNING, PAUSING, HALTED;
    }

    State state = State.HALTED;

    final ActionListener loadAction = e -> loadFile(this.engine::process, "Run FORTH source file");
    final ActionListener runAction = e -> loadFile(this.input::appendLine, "Load FORTH source file");
    final ActionListener saveAction = e -> saveFile(this.input.getText(), "Save FORTH source file");

    private JFrame frame = new JFrame("Forth");
    InConsole input = new InConsole().with(s -> process(s));

    Inspectable engine;
    private DictionaryPanel dictionaryPanel;

    private ThreadControl threadControl;

    private StackPanel dataStackPanel;
    private StackPanel returnStackPanel;
    private PipedConsole errOutConsole;
    private PluggedConsole engineOutConsole;
    private JPanel inputConsole;
    private DebugPanel debugPanel;
    private StatusBar statusBar;
    private int fontsize;

    /**
     * Build the Window.
     *
     * @param engine
     *            forth engine to use
     * @param height
     *            height of the main windows
     * @param width
     *            wifth of the main windows
     */
    public ForthGui(Inspectable engine, int width, int height) {

        this.engine = engine;
        this.threadControl = new ThreadControl(this, engine);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(width, height));

        // populate the frame
        JSplitPane consoleAndDictionaryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, //
                mkConsolePanel(), //
                mkDictionaryPanel());

        frame.getContentPane().add(mkStackPanel(), BorderLayout.WEST);
        frame.getContentPane().add(consoleAndDictionaryPane, BorderLayout.CENTER);
        frame.getContentPane().add(statusBar = new StatusBar(engine), BorderLayout.SOUTH);

        frame.setJMenuBar(new ForthGuiMenu(this));

        frame.pack();
        frame.setVisible(true);

        setFontSize(12);
        setState(State.HALTED);

        engine.getDictionary()
                .add(new UserVariableWord("fontsize", () -> this.fontsize, size -> this.setFontSize(size)));
    }

    void setFontSize(int size) {
        this.fontsize = size;
        setFont(new Font(General.FONT_NAME, Font.PLAIN, size));
    }

    public Inspectable getEngine() {
        return engine;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        refresh();
    }

    void setFont(Font font) {
        Stream.of(dataStackPanel, returnStackPanel, //
                debugPanel, //
                engineOutConsole, errOutConsole, //
                input, dictionaryPanel)//
                .filter(Objects::nonNull)//
                .forEach(c -> c.setFont(font));
    }

    /**
     * Creates a panel for the dictionary and debug panels.
     *
     * @return combined panel
     */
    private JComponent mkDictionaryPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, //
                dictionaryPanel = new DictionaryPanel(engine), //
                debugPanel = new DebugPanel(this) //
        );
        return splitPane;
    }

    /**
     * Creates a panel with I/O-consoles and buttons.
     *
     * @return combined Panel
     */
    private JPanel mkConsolePanel() {

        // create and connect area-Console
        engineOutConsole = new PluggedConsole(frame, engine);

        // create and connect Standard Err inputConsole
        PipedOutputStream errOut = new PipedOutputStream();
        System.setErr(new PrintStream(errOut, true));
        errOutConsole = new PipedConsole(frame, errOut);

        JTabbedPane outputPane = new JTabbedPane();
        outputPane.addTab("Output", null, mkVerticalScrollPane(engineOutConsole), "engine output");
        outputPane.addTab("Error", null, mkVerticalScrollPane(errOutConsole), "standard error");

        JSplitPane ioPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, //
                outputPane, //
                new JScrollPane(input) //
        );

        // Button-Panel for I/O
        JPanel btnPane = new JPanel();
        btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.LINE_AXIS));
        btnPane.add(General.mkButton("Out", e -> engineOutConsole.clear(), ButtonIcon.CLEAR_OUT));
        btnPane.add(General.mkButton("Err", e -> errOutConsole.clear(), ButtonIcon.CLEAR_ERR));
        btnPane.add(General.mkButton("In", e -> input.clear(), ButtonIcon.CLEAR_IN));
        btnPane.add(General.mkButton("", e -> refresh(), ButtonIcon.REFRESH));
        btnPane.add(General.mkButton("run", loadAction, ButtonIcon.RUN_FILE));
        btnPane.add(General.mkButton("load", runAction, ButtonIcon.LOAD_FILE));

        // input panel
        inputConsole = new JPanel();
        inputConsole.setLayout(new BoxLayout(inputConsole, BoxLayout.PAGE_AXIS));
        ioPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputConsole.add(ioPane);
        btnPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputConsole.add(btnPane);
        return inputConsole;
    }

    /**
     * Create a panel for data and return stack.
     *
     * @return combined panel
     */
    private JComponent mkStackPanel() {
        JPanel stacks = new JPanel();
        stacks.setLayout(new BoxLayout(stacks, BoxLayout.PAGE_AXIS));
        stacks.add(dataStackPanel = new StackPanel(engine.getDataStack(), "Data Stack", engine::getBase));
        stacks.add(
                returnStackPanel = new StackPanel(engine.getReturnStack().content(), "Return Stack", engine::getBase));
        stacks.setPreferredSize(new Dimension(200, 100));
        return stacks;
    }

    /**
     * Open a dialog to load source from a file and read it.
     *
     * @param consumer
     *            consume a line from the file
     * @param title
     *            dialog title
     */
    void loadFile(Consumer<String> consumer, String title) {
        final JFileChooser fc = new JFileChooser();
        String fileDir = Access.get().get(Props.FILE_DIR);
        if (!Util.isEmpty(fileDir)) {
            fc.setCurrentDirectory(new File(fileDir));
        }
        fc.setDialogTitle(title);
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            try (Stream<String> stream = Files.lines(Paths.get(selectedFile.getPath()))) {
                stream.forEach(consumer);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        refresh();
    }

    void saveFile(String content, String title) {
        final JFileChooser fc = new JFileChooser();
        String fileDir = Access.get().get(Props.FILE_DIR);
        if (!Util.isEmpty(fileDir)) {
            fc.setCurrentDirectory(new File(fileDir));
        }
        fc.setDialogTitle(title);
        if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            try (FileWriter fw = new FileWriter(selectedFile)) {
                fw.write(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void go() {
        if (state != State.RUNNING) {
            threadControl.startExecution(input.getSelection());
        }
    }

    public void stepOut() {
        threadControl.stepOut();
    }

    public void stepOver() {
        threadControl.stepOver();
    }

    public void pause() {
        threadControl.pauseExecution();
    }

    public void stop() {
        threadControl.stopExecution();
    }

    public void stepInto() {
        threadControl.stepExecution(input.getSelection());
    }

    public void reset() {
        engine.reset(false);
        refresh();
    }

    public void close() {
        frame.dispose();
    }

    @Override
    public void refresh() {
        doIfNotNull(dataStackPanel, Refreshable::refresh);
        doIfNotNull(returnStackPanel, Refreshable::refresh);
        doIfNotNull(dictionaryPanel, Refreshable::refresh);
        doIfNotNull(debugPanel, Refreshable::refresh);
        doIfNotNull(statusBar, Refreshable::refresh);
    }

    /**
     * Run the command string with the engine.
     *
     * @param command
     *            command to execute
     */
    public void process(String command) {
        if (state == State.HALTED) {
            threadControl.startExecution(command);
        }
    }

}
