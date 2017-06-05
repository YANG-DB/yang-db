package com.kayhut.fuse.generator;

import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.data.generation.entity.DragonGenerator;
import com.kayhut.fuse.generator.model.entity.Dragon;
import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by benishue on 15-May-17.
 */
public class DragonGeneratorTest {

    static final String CONFIGURATION_FILE_PATH = "test.generator.properties";
    static Configuration configuration;
    static final int NUM_OF_DRAGONS = 100;

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
    public static void setup() throws Exception {
        configuration = new DataGenConfiguration(CONFIGURATION_FILE_PATH).getInstance();
    }

}