package io.github.mletkin.jemforth.gui;

import java.awt.FlowLayout;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * Radio button group for number base selection.
 */
public class BaseSelectPanel extends JPanel {

    private JToggleButton dec = General.mkToggleButton(e -> this.setBase(10), ButtonIcon.DECIMAL);
    private JToggleButton hex = General.mkToggleButton(e -> this.setBase(16), ButtonIcon.HEX);
    private JToggleButton bin = General.mkToggleButton(e -> this.setBase(2), ButtonIcon.BINARY);
    private JToggleButton eng = General.mkToggleButton(e -> this.setBase(0), ButtonIcon.ENGINE_BASE);

    private int base;

    private Supplier<Integer> baseSupplier;
    private Consumer<Integer> baseChanged;

    public BaseSelectPanel(Supplier<Integer> baseSupplier, Consumer<Integer> baseChanged) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.baseSupplier = baseSupplier;
        this.baseChanged = baseChanged;

        this.add(dec);
        this.add(hex);
        this.add(bin);
        this.add(eng);
        setBase(0);
    }

    private void setBase(int base) {
        this.base = base;
        dec.setSelected(base == 10);
        bin.setSelected(base == 2);
        hex.setSelected(base == 16);
        eng.setSelected(base == 0);
        baseChanged.accept(getBase());
    }

    public int getBase() {
        return base == 0 ? baseSupplier.get() : base;
    }
}
