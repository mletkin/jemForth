package io.github.mletkin.jemforth.gui.settings;

import java.util.Properties;

public class AppProperties extends Properties {

    public String get(Props propName) {
        return get(propName.id).toString();
    }
}
