package com.kayhut.fuse.generator;

import com.google.common.collect.LinkedHashMultiset;
import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.configuration.GuildConfiguration;
import com.kayhut.fuse.generator.configuration.KingdomConfiguration;
import com.kayhut.fuse.generator.data.generation.GuildsGraphGenerator;
import com.kayhut.fuse.generator.helper.TestUtil;
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
import java.util.Map;
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
    private final static int NUM_OF_PERSONS = 1000;
    private final static int NUM_OF_GUILDS = 100;
    static Configuration configuration;
    static Logger logger;

    @Test
    public void generateGuildsTest() throws Exception {
        GuildsGraphGenerator guildsGraphGenerator = new GuildsGraphGenerator(configuration);
        List<Guild> guilds = guildsGraphGenerator.generateGuilds();
        GuildConfiguration guildConfiguration = new GuildConfiguration(configuration);
        assertEquals(guildConfiguration.getNumberOfNodes(), guilds.size());
        assertEquals("0", guilds.get(0).getId());
        assertTrue(Arrays.asList(guildConfiguration.getGuilds()).
                contains(RandomUtil.getRandomElementFromList(guilds).getName()));
    }


    @Test
    public void attachPersonsToGuildsTest() throws Exception {
        List<String> personsIdList = IntStream.rangeClosed(0, NUM_OF_PERSONS - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        List<String> guildsIdList = IntStream.rangeClosed(0, NUM_OF_GUILDS - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        GuildsGraphGenerator guildsGraphGenerator = new GuildsGraphGenerator(configuration);
        Map<String, List<String>> personsToGuildsEdges = guildsGraphGenerator.attachPersonsToGuilds(guildsIdList, personsIdList);


        //Check there are no duplicates
        for (Map.Entry<String, List<String>> entry : personsToGuildsEdges.entrySet()) {
            List<String> listOfMembers = entry.getValue();
            assertFalse(TestUtil.hasDuplicate(listOfMembers));
            //System.out.println("Guild Id=" + entry.getKey() + ", #Person: " + entry.getValue().size());
        }


        //Checking that the size of the EdgeSet is the size of persons population less 0.025% (not belong to any guild)
        //assertEquals(9750, personsToGuildsEdges.size());

//        List<Integer> personsIdsInEdges = Stream.ofAll(personsToGuildsEdges).map(tuple2 -> (Integer) tuple2._1).toJavaList();
//        List<Integer> guildsIdsInEdges = Stream.ofAll(personsToGuildsEdges).map(tuple2 -> (Integer) tuple2._2).toJavaList();

        //Check that all Persons belong to kingdoms
//        for (int i = 0; i < 9750; i++) {
//            assertThat(personsIdsInEdges, hasItem(i));
//        }
//
//
//
//        personsToGuildsEdges.stream()
//                .collect(Collectors.groupingBy(tuple2 -> tuple2._2, Collectors.counting()))
//                .forEach((id, count) -> System.out.println("Guild id = " + id + "\t" + "Count: " + count));

    }

   @BeforeClass
    public static void setup() throws Exception {
        configuration = new DataGenConfiguration(CONFIGURATION_FILE_PATH).getInstance();
        logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
    }

    //region Private Methods

    //endregion

}