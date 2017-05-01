package com.kayhut.fuse.stat.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.Iterator;

/**
 * Created by benishue on 30-Apr-17.
 */
public class StatisticsConfiguration {

    private static Configuration configuration;

    public StatisticsConfiguration(String configPath) {
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
