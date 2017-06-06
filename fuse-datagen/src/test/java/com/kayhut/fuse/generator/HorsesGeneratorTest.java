package com.kayhut.fuse.generator;

import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.configuration.GuildConfiguration;
import com.kayhut.fuse.generator.configuration.HorseConfiguration;
import com.kayhut.fuse.generator.data.generation.GuildsGraphGenerator;
import com.kayhut.fuse.generator.data.generation.HorsesGraphGenerator;
import com.kayhut.fuse.generator.data.generation.entity.DragonGenerator;
import com.kayhut.fuse.generator.data.generation.entity.HorseGenerator;
import com.kayhut.fuse.generator.helper.TestUtil;
import com.kayhut.fuse.generator.model.entity.Dragon;
import com.kayhut.fuse.generator.model.entity.Guild;
import com.kayhut.fuse.generator.model.entity.Horse;
import com.kayhut.fuse.generator.util.RandomUtil;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Created by benishue on 21-May-17.
 */
public class HorsesGeneratorTest {

    static final String CONFIGURATION_FILE_PATH = "test.generator.properties";
    private final static int NUM_OF_PERSONS = 1000;
    private final static int NUM_OF_HORSES = 6500;
    static Configuration configuration;
    static Logger logger;

    @Test
    public void generateHorsesTest() throws Exception {
        HorsesGraphGenerator horsesGraphGenerator = new HorsesGraphGenerator(new HorseConfiguration(configuration));
        List<Horse> horses = horsesGraphGenerator.generateHorses();
        HorseConfiguration horseConfiguration = new HorseConfiguration(configuration);
        assertEquals(horseConfiguration.getNumberOfNodes(), horses.size());
        assertEquals("0", horses.get(0).getId());
    }

    @Test
    public void generateHorseTest() throws Exception {
        HorseConfiguration horseConfiguration = new HorseConfiguration(configuration);
        HorseGenerator horseGenerator = new HorseGenerator(horseConfiguration);
        for (int i= 0 ; i < NUM_OF_HORSES ; i++) {
            Horse horse = horseGenerator.generate();
            horse.setId(Integer.toString(i));
            assertTrue(horse.getMaxSpeed() <= horseConfiguration.getMaxSpeed());
            assertTrue(horse.getMaxSpeed() >= horseConfiguration.getMinSpeed());
            assertTrue(!horse.getName().isEmpty());
        }
    }
   @BeforeClass
    public static void setup() throws Exception {
        configuration = new DataGenConfiguration(CONFIGURATION_FILE_PATH).getInstance();
        logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
    }

    //region Private Methods

    //endregion

}