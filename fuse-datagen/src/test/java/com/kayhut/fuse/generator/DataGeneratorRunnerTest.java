package com.kayhut.fuse.generator;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by benishue on 22-May-17.
 */
public class DataGeneratorRunnerTest {

    static final String CONFIGURATION_FILE_PATH = "test.generator.properties";

    @Test
    public void dataGenerationTest() throws Exception {
        DataGenerator.main(new String [] {CONFIGURATION_FILE_PATH});
    }

    @BeforeClass
    public static void setUp() throws Exception {
        DataGenerator.loadConfiguration(CONFIGURATION_FILE_PATH);
    }
}