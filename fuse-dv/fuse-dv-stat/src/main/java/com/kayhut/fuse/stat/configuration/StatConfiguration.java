package com.kayhut.fuse.stat.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created by benishue on 30-Apr-17.
 */
public class StatConfiguration {

    private Configuration configuration;

    public StatConfiguration(String configPath) throws Exception {
        configuration = setConfiguration(configPath);
    }

    private synchronized Configuration setConfiguration(String configPath) throws Exception {
        if (configuration != null) {
            return configuration;
        }
        configuration = new PropertiesConfiguration(configPath);

        return configuration;
    }

    public Configuration getInstance() {
        return configuration;
    }
}
