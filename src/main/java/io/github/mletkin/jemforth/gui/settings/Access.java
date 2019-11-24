package io.github.mletkin.jemforth.gui.settings;

import java.io.IOException;
import java.io.InputStream;

/**
 * access to the Forth GUI properties.
 */
public class Access {

    private static final String DEFAULT_PROP_FILE = "config.properties";

    /**
     * Get the properties
     *
     * @return {@link AppProperties} instance containing the properties
     */
    public static AppProperties get() {
        return (new Access()).readDefault();
    }

    /**
     * Get the default prop file from the resources
     *
     * @return {@link AppProperties} instance containing the properties
     */
    AppProperties readDefault() {
        AppProperties prop = new AppProperties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(DEFAULT_PROP_FILE)) {
            if (input == null) {
                System.err.println("Sorry, unable to find " + DEFAULT_PROP_FILE);
                return prop;
            }

            // load a properties file from class path, inside static method
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }
}
