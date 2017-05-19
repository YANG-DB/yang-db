package com.kayhut.fuse.generator.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created by benishue on 15-May-17.
 */
public class DataGenConfiguration {

    private static Configuration configuration;

    public DataGenConfiguration(String configPath) {
        configuration = setConfiguration(configPath);
    }

    private synchronized Configuration setConfiguration(String configPath) {
        try {
            if (configuration != null) {
                return configuration;
            }
            configuration = new PropertiesConfiguration(configPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return configuration;
    }

    public Configuration getInstance()
    {
        return configuration;
    }
}
