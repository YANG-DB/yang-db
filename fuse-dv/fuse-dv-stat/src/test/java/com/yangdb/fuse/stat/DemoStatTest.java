package com.yangdb.fuse.stat;

import com.yangdb.fuse.stat.configuration.StatConfiguration;
import org.junit.Ignore;
import org.junit.Test;

import static com.yangdb.fuse.stat.StatTestSuite.dataClient;
import static com.yangdb.fuse.stat.StatTestSuite.statClient;

/**
 * Created by benishue on 07-Jun-17.
 */
public class DemoStatTest {

    private static final String CONFIGURATION_FILE_PATH = "statistics.demo.properties";

    @Ignore
    @Test
    public void runDemo() throws Exception {
        StatCalculator.run(dataClient, statClient, new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance());
    }
}
