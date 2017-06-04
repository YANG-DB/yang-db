package com.kayhut.fuse.generator;

import com.google.common.collect.LinkedHashMultiset;
import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.configuration.GuildConfiguration;
import com.kayhut.fuse.generator.configuration.KingdomConfiguration;
import com.kayhut.fuse.generator.model.entity.Guild;
import com.kayhut.fuse.generator.model.entity.Kingdom;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

/**
 * Created by benishue on 21-May-17.
 */
public class GuildsGeneratorTest {

    static final String CONFIGURATION_FILE_PATH = "test.generator.properties";
    static Configuration configuration;
    static Logger logger;

    @Test
    public void generateGuildsTest() throws Exception {
        List<Guild> guilds = DataGenerator.generateGuilds(configuration);
        GuildConfiguration guildConfiguration = new GuildConfiguration(configuration);
        assertEquals(guildConfiguration.getNumberOfNodes(), guilds.size());
        assertEquals("0", guilds.get(0).getId());
        assertTrue(Arrays.asList(guildConfiguration.getGuilds()).
                contains(RandomUtil.getRandomElementFromList(guilds).getName()));
    }

   @BeforeClass
    public static void setup() throws Exception {
        configuration = new DataGenConfiguration(CONFIGURATION_FILE_PATH).getInstance();
        logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
    }

    //region Private Methods

    //endregion

}