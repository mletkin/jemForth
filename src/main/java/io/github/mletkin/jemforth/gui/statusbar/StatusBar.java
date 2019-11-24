package io.github.mletkin.jemforth.gui.statusbar;

import java.awt.FlowLayout;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import io.github.mletkin.jemforth.engine.Inspectable;
import io.github.mletkin.jemforth.engine.StringWord;
import io.github.mletkin.jemforth.engine.UserVariableWord;
import io.github.mletkin.jemforth.engine.Util;
import io.github.mletkin.jemforth.gui.Refreshable;

/**
 * Status bar for the display of user variables and strings.
 */
public class StatusBar extends JPanel implements Refreshable {

    // keep references for refresh
    private List<DataField> variables;
    private List<DataField> strings;

    /**
     * Set up the status bar.
     *
     * @param engine
     *            engine to use
     */
    public StatusBar(Inspectable engine) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(mkFieldLine(variables = getVariables(engine)));
        add(mkFieldLine(strings = getStrings(engine)));
    }

    /**
     * Create a panel with all data fields from the list.
     *
     * @param list
     *            list of {@link DataField} objects to dislay
     * @return panel displaying the data fields
     */
    private JPanel mkFieldLine(List<DataField> list) {
        JPanel line = new JPanel(new FlowLayout());
        list.stream() //
                .forEach(p -> {
                    line.add(p.label);
                    line.add(p.field);
                });
        return line;
    }

    /**
     * Get a {@link DataField} object for each defined user variable.
     *
     * @param engine
     *            engine containing the variables
     * @return list of DataFields representing the variables
     */
    private List<DataField> getVariables(Inspectable engine) {
        return engine.getDictionary().memory().stream() //
                .filter(w -> w instanceof UserVariableWord) //
                .map(p -> new VariableField(p)) //
                .collect(Collectors.toList());
    }

    /**
     * Get a {@link DataField} object for each defined string.
     *
     * @param engine
     *            engine containing the strings
     * @return list of DataFields representing the strings
     */
    private List<DataField> getStrings(Inspectable engine) {
        return engine.getDictionary().memory().stream() //
                .filter(w -> w instanceof StringWord) //
                .filter(w -> !Util.isEmpty(w.name())) //
                .map(p -> new StringField(p)).collect(Collectors.toList());
    }

    @Override
    public void refresh() {
        Stream.concat(variables.stream(), strings.stream()).forEach(DataField::refresh);
        validate();
    }
}
