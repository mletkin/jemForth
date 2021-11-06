package io.github.mletkin.jemforth.gui;

public enum Image {

    CLEAR("clear.gif"),
    RESUME("resume.gif"),
    STOP("stop.gif"),
    RESET("reset.gif"),
    REFRESH("refresh.gif"),
    EMPTY("empty.gif"),
    HASH("hash.png"),
    DOLLAR("dollar.png"),
    PERCENT("percent.png"),
    F("f.png"),
    PAUSE("suspend.gif"),
    OPEN("open.gif"),
    STEP_OVER("stepover.gif"),
    STEP_INTO("stepinto.gif"),
    STEP_OUT("stepout.gif"),

    ;

    public final String path;

    Image(String path) {
        this.path = "/" + path;
    };

}
