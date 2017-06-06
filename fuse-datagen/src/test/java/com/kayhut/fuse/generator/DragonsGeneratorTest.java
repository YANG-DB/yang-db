package com.kayhut.fuse.generator;

import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.data.generation.DragonsGraphGenerator;
import com.kayhut.fuse.generator.data.generation.entity.DragonGenerator;
import com.kayhut.fuse.generator.helper.TestUtil;
import com.kayhut.fuse.generator.model.entity.Dragon;
import com.kayhut.fuse.generator.model.enums.RelationType;
import com.kayhut.fuse.generator.util.CsvUtil;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 15-May-17.
 */
public class DragonsGeneratorTest {

    static final String CONFIGURATION_SMALL_FILE_PATH = "test.generator.properties";
    static final String CONFIGURATION_MASSIVE_FILE_PATH = "test.generator.massive.properties";

    static final int NUM_OF_DRAGONS = 100;

    static Configuration configuration;
    static DragonConfiguration dragonConfiguration;
    static String dragonsFilePath;
    static String dragonsFireRelationFilePath;
    static String dragonsFreezRelationFilePath;
    static Logger logger;

    @Test
    public void testDragonsDataGenerationSmallGraph() throws Exception {
        loadConfigurations(CONFIGURATION_SMALL_FILE_PATH);
        File dragonsFile = new File(dragonsFilePath);
        Files.deleteIfExists(dragonsFile.toPath());
        DragonsGraphGenerator dragonsGraphGenerator = new DragonsGraphGenerator(dragonConfiguration);
        dragonsGraphGenerator.generateSmallDragonsGraph(configuration.getString("resultsPath"), false);
        assertTrue(TestUtil.isFileExists(dragonsFilePath));
        List<String[]> dragonsLines = CsvUtil.readCSV(dragonsFilePath, ',');
        assertTrue(dragonsLines.size() > dragonConfiguration.getNumberOfNodes() - 1);
        assertTrue(dragonsLines.get(0)[0].equals("0")); //[0] = graph Id
        assertTrue(dragonsLines.get(1)[0].equals("1")); // No weird 'Jumps'
    }

    @Test
    public void testDragonsDataGenerationMassiveGraph() throws Exception {
        loadConfigurations(CONFIGURATION_MASSIVE_FILE_PATH);
        File dragonsFile = new File(dragonsFilePath);
        Files.deleteIfExists(dragonsFile.toPath());
        DragonsGraphGenerator dragonsGraphGenerator = new DragonsGraphGenerator(dragonConfiguration);
        dragonsGraphGenerator.generateMassiveDragonsGraph();
        assertTrue(TestUtil.isFileExists(dragonsFilePath));
        List<String[]> dragonsLines = CsvUtil.readCSV(dragonsFilePath, ',');
        assertTrue(dragonsLines.size() > dragonConfiguration.getNumberOfNodes() - 1);
        assertTrue(dragonsLines.get(0)[0].equals("0")); //[0] = graph Id
        assertTrue(dragonsLines.get(1)[0].equals("1")); // No weird 'Jumps'
    }

    @Test
    public void generateDragonTest() throws Exception {
        DragonConfiguration dragonConfiguration = new DragonConfiguration(configuration);
        DragonGenerator dragonGenerator = new DragonGenerator(dragonConfiguration);
        for (int i= 0 ; i < NUM_OF_DRAGONS ; i++) {
            Dragon dragonA = dragonGenerator.generate();
            dragonA.setId(Integer.toString(i));
            assertTrue(dragonA.getPower() >= dragonConfiguration.getMinPower() && dragonA.getPower() <= dragonConfiguration.getMaxPower());
            assertTrue(!dragonA.getName().isEmpty());
            //System.out.println(dragonA);
        }

    }

    @BeforeClass
    public static void setUp() throws Exception {
        logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
    }

    private static void loadConfigurations(String configurationFilePath) {
        configuration = new DataGenConfiguration(configurationFilePath).getInstance();
        dragonConfiguration = new DragonConfiguration(configuration);
        dragonsFilePath = dragonConfiguration.getEntitiesFilePath();
        dragonsFireRelationFilePath = dragonConfiguration.getRelationsFilePath().replace(".csv","") + "_" + RelationType.FIRES + ".csv";
        dragonsFreezRelationFilePath = dragonConfiguration.getRelationsFilePath().replace(".csv","") + "_" + RelationType.FREEZES + ".csv";
    }



}