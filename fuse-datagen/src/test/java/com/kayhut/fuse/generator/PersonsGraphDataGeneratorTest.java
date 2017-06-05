package com.kayhut.fuse.generator;

import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.configuration.PersonConfiguration;
import com.kayhut.fuse.generator.helper.TestUtil;
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
public class PersonsGraphDataGeneratorTest {

    static String CONFIGURATION_SMALL_FILE_PATH = "test.generator.properties";
    static String CONFIGURATION_MASSIVE_FILE_PATH = "test.generator.massive.properties";

    static Configuration configuration;
    static PersonConfiguration personConfiguration;
    static String personsFilePath;
    static String personsKnowsRelationFilePath;
    static Logger logger;


    @Test
    public void testPersonsDataGenerationMassiveGraph() throws Exception {
        loadConfigurations(CONFIGURATION_MASSIVE_FILE_PATH);
        File personsFile = new File(personsFilePath);
        Files.deleteIfExists(personsFile.toPath());
        DataGenerator.generateMassivePersonsGraph(configuration);
        assertTrue(TestUtil.isFileExists(personsFilePath));
        List<String[]> dragonsLines = CSVUtil.readCSV(personsFilePath, ',');
        assertTrue(dragonsLines.size() > personConfiguration.getNumberOfNodes() - 1);
        assertTrue(dragonsLines.get(0)[0].equals("0")); //[0] = graph Id
        assertTrue(dragonsLines.get(1)[0].equals("1")); // No weird 'Jumps'
    }



    @BeforeClass
    public static void setUp() throws Exception {
        logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
    }

    private static void loadConfigurations(String configurationFilePath) {
        configuration = new DataGenConfiguration(configurationFilePath).getInstance();
        personConfiguration = new PersonConfiguration(configuration);
        personsFilePath = personConfiguration.getEntitiesFilePath();
        personsKnowsRelationFilePath = personConfiguration.getRelationsFilePath().replace(".csv","") + "_" + RelationType.KNOWS + ".csv";
    }
}