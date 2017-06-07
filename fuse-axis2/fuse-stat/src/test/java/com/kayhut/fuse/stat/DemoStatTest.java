package com.kayhut.fuse.stat;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by benishue on 07-Jun-17.
 */
public class DemoStatTest {

    private static final String CONFIGURATION_FILE_PATH = "statistics.demo.properties";

    @Ignore
    @Test
    public void runDemo() throws Exception {
        StatCalculator.main(new String[]{CONFIGURATION_FILE_PATH});
    }
}
