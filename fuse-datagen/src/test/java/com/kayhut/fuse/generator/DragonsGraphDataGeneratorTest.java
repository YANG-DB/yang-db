package com.kayhut.fuse.generator;

import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.model.enums.RelationType;
import com.kayhut.fuse.generator.util.CSVUtil;
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
public class DragonsGraphDataGeneratorTest {

    static String CONFIGURATION_SMALL_FILE_PATH = "test.generator.properties";
    static String CONFIGURATION_MASSIVE_FILE_PATH = "test.generator.massive.properties";

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
        DataGenerator.generateSmallDragonsGraph(logger, configuration, false);
        assertTrue(isFileExists(dragonsFilePath));
        List<String[]> dragonsLines = CSVUtil.readCSV(dragonsFilePath, ',');
        assertTrue(dragonsLines.size() > dragonConfiguration.getNumberOfNodes() - 1);
        assertTrue(dragonsLines.get(0)[0].equals("0")); //[0] = dragon Id
        assertTrue(dragonsLines.get(1)[0].equals("1")); // No weird 'Jumps'
    }


    @Test
    public void testDragonsDataGenerationMassiveGraph() throws Exception {
        loadConfigurations(CONFIGURATION_MASSIVE_FILE_PATH);
        File dragonsFile = new File(dragonsFilePath);
        Files.deleteIfExists(dragonsFile.toPath());
        DataGenerator.generateMassiveDragonsGraph(logger, configuration);
        assertTrue(isFileExists(dragonsFilePath));
        List<String[]> dragonsLines = CSVUtil.readCSV(dragonsFilePath, ',');
        assertTrue(dragonsLines.size() > dragonConfiguration.getNumberOfNodes() - 1);
        assertTrue(dragonsLines.get(0)[0].equals("0")); //[0] = dragon Id
        assertTrue(dragonsLines.get(1)[0].equals("1")); // No weird 'Jumps'
    }



    @BeforeClass
    public static void setUp() throws Exception {
        logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
    }

    private static void loadConfigurations(String configurationFilePath) {
        configuration = new DataGenConfiguration(configurationFilePath).getInstance();
        dragonConfiguration = new DragonConfiguration(configuration);
        dragonsFilePath = dragonConfiguration.getDragonsResultsFilePath();
        dragonsFireRelationFilePath = configuration.getString("resultsPath") + "//" + configuration.getString("dragon.dragonsResultsCsvFileName");
        dragonsFireRelationFilePath = dragonConfiguration.getDragonsRelationsFilePath().replace(".csv","") + "_" + RelationType.FIRES + ".csv";
        dragonsFreezRelationFilePath = dragonConfiguration.getDragonsRelationsFilePath().replace(".csv","") + "_" + RelationType.FREEZES + ".csv";
    }

    private static boolean isFileExists(String filePath){
        File file = new File(filePath);
        return file.exists();
    }

}