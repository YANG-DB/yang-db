package com.kayhut.fuse.generator;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.kayhut.fuse.generator.data.generation.KingdomsGraphGenerator;
import com.kayhut.fuse.generator.helper.TestUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

/**
 * Created by benishue on 22-May-17.
 */
public class DataGeneratorTest {

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