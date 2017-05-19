package com.kayhut.fuse.generator;

import com.kayhut.fuse.generator.generator.dragon.DragonConfiguration;
import com.kayhut.fuse.generator.generator.dragon.DragonGenerator;
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


    @Test
    public void generateDragon() throws Exception {
        DragonGenerator dragonGenerator = new DragonGenerator(new DragonConfiguration(configuration));

        for (int i= 0 ; i <100 ; i++) {
            Dragon dragonA = dragonGenerator.generateDragon();
            assertTrue(dragonA.getPower() >= 10 && dragonA.getPower() <= 100);
            assertTrue(!dragonA.getName().isEmpty());
            System.out.println(dragonA);
        }

    }
    @BeforeClass
    public static void setup() throws Exception {

        configuration = new DataGenConfiguration(CONFIGURATION_FILE_PATH).getInstance();
    }
}