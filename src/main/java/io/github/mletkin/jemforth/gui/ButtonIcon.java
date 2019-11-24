package io.github.mletkin.jemforth.gui;

import javax.swing.ImageIcon;

public enum ButtonIcon {

    CLEAR_IN(Image.CLEAR, "clear input console"),
    CLEAR_OUT(Image.CLEAR, "clear output console"),
    CLEAR_ERR(Image.CLEAR, "clear error console"),
    RESUME(Image.RESUME, "run engine"),
    STEP_INTO(Image.STEP_INTO, "step into"),
    STEP_OVER(Image.STEP_OVER, "step over"),
    STEP_OUT(Image.STEP_OUT, "step out"),
    STOP(Image.STOP, "stop engine"),
    RESET(Image.RESET, "reset engine"),
    REFRESH(Image.REFRESH, "refresh display"),
    EMPTY(Image.EMPTY, "empty stack"),
    DECIMAL(Image.HASH, "display decimal"),
    HEX(Image.DOLLAR, "display hex"),
    BINARY(Image.PERCENT, "display binary"),
    ENGINE_BASE(Image.F, "display with engine base"),
    SUSPEND(Image.PAUSE, "pause execution"),
    LOAD_FILE(Image.OPEN, "read source from file"),
    RUN_FILE(Image.OPEN, "run source from file"),

    ;

    private ImageIcon imageIcon;

    private ButtonIcon(Image icon, String description) {
        java.net.URL imgURL = General.class.getResource(icon.path);
        if (imgURL != null) {
            imageIcon = new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + icon.path);
        }
    }

    public String tip() {
        return imageIcon.getDescription();
    }

    public ImageIcon image() {
        return imageIcon;
    }

}
