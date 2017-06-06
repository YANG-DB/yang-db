package com.kayhut.fuse.generator;

import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.configuration.KingdomConfiguration;
import com.kayhut.fuse.generator.data.generation.KingdomsGraphGenerator;
import com.kayhut.fuse.generator.helper.TestUtil;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

/**
 * Created by benishue on 21-May-17.
 */
public class KingdomsGeneratorTest {

    static final String CONFIGURATION_FILE_PATH = "test.generator.properties";
    private final static int NUM_OF_PERSONS = 1000;
    private final static int NUM_OF_KINGDOMS = 8;
    private final static int NUM_OF_GUILDS = 100;
    private final static int NUM_OF_DRAGONS = 10000;
    private final static int NUM_OF_HORSES = 6500;
    static Configuration configuration;
    static Logger logger;

    @Test
    public void generateKingdomsTest() throws Exception {
        KingdomsGraphGenerator kingdomsGraphGenerator = new KingdomsGraphGenerator(new KingdomConfiguration(configuration));
        List<Kingdom> kingdoms = kingdomsGraphGenerator.generateKingdoms();
        KingdomConfiguration kingdomConfiguration = new KingdomConfiguration(configuration);
        assertEquals(kingdomConfiguration.getNumberOfNodes(), kingdoms.size());
        assertEquals("0", kingdoms.get(0).getId());
        assertTrue(Arrays.asList(kingdomConfiguration.getKingdoms()).
                contains(RandomUtil.getRandomElementFromList(kingdoms).getName()));
    }

    @Test
    public void attachPersonsToKingdomsTest() throws Exception {
        List<String> personsIdList = IntStream.rangeClosed(0, NUM_OF_PERSONS - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        List<String> kingdomsIdList = IntStream.rangeClosed(0, NUM_OF_KINGDOMS - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        KingdomsGraphGenerator kingdomsGraphGenerator = new KingdomsGraphGenerator(new KingdomConfiguration(configuration));
        List<Tuple2> personsToKingdomsEdges = kingdomsGraphGenerator.attachPersonToKingdoms(kingdomsIdList, personsIdList);
        //Checking that the size of the EdgeSet is the size of persons list
        assertEquals(personsIdList.size(), personsToKingdomsEdges.size());

        List<Integer> personsIdsInEdges = Stream.ofAll(personsToKingdomsEdges).map(tuple2 -> (Integer) tuple2._1).toJavaList();

        List<Integer> kingdmsIdsInEdges = Stream.ofAll(personsToKingdomsEdges).map(tuple2 -> (Integer) tuple2._2).toJavaList();

        //Check that all Persons belong to kingdoms
        for (int i = 0; i < personsIdList.size(); i++) {
            assertThat(personsIdsInEdges, hasItem(i));
        }


        for (int i = 0; i < kingdomsIdList.size(); i++) {
            assertThat(kingdmsIdsInEdges, hasItem(i));
        }

        assertEquals(TestUtil.findDuplicates(personsIdsInEdges).size(), 0);
    }

    @Test
    public void attachHorsesToKingdomsTest() throws Exception {
        List<String> horsesIdList = IntStream.rangeClosed(0, NUM_OF_HORSES - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        List<String> kingdomsIdList = IntStream.rangeClosed(0, NUM_OF_KINGDOMS - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        KingdomsGraphGenerator kingdomsGraphGenerator = new KingdomsGraphGenerator(new KingdomConfiguration(configuration));
        List<Tuple2> horsesToKingdomsEdges = kingdomsGraphGenerator.attachHorseToKingdom(kingdomsIdList, horsesIdList);
        //Checking that the size of the EdgeSet is the size of horses list
        assertEquals(horsesIdList.size(), horsesToKingdomsEdges.size());

        List<Integer> horsesIdsInEdges = Stream.ofAll(horsesToKingdomsEdges).map(tuple2 -> (Integer) tuple2._1).toJavaList();

        List<Integer> kingdmsIdsInEdges = Stream.ofAll(horsesToKingdomsEdges).map(tuple2 -> (Integer) tuple2._2).toJavaList();

        //Check that all horses belong to kingdoms
        for (int i = 0; i < horsesIdList.size(); i++) {
            assertThat(horsesIdsInEdges, hasItem(i));
        }


        for (int i = 0; i < kingdomsIdList.size(); i++) {
            assertThat(kingdmsIdsInEdges, hasItem(i));
        }

        assertEquals(TestUtil.findDuplicates(horsesIdsInEdges).size(), 0);
    }

    @Test
    public void attachGuildsToKingdomsTest() throws Exception {
        List<String> guildsIdList = IntStream.rangeClosed(0, NUM_OF_GUILDS - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        List<String> kingdomsIdList = IntStream.rangeClosed(0, NUM_OF_KINGDOMS - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        KingdomsGraphGenerator kingdomsGraphGenerator = new KingdomsGraphGenerator(new KingdomConfiguration(configuration));
        List<Tuple2> guildsToKingdomsEdges = kingdomsGraphGenerator.attachGuildToKingdom(kingdomsIdList, guildsIdList);
        //Checking that the size of the EdgeSet is the size of horses list
        assertEquals(guildsIdList.size(), guildsToKingdomsEdges.size());

        List<Integer> guildIdsInEdges = Stream.ofAll(guildsToKingdomsEdges).map(tuple2 -> (Integer) tuple2._1).toJavaList();

        List<Integer> kingdmsIdsInEdges = Stream.ofAll(guildsToKingdomsEdges).map(tuple2 -> (Integer) tuple2._2).toJavaList();

        //Check that all guilds belong to kingdoms
        for (int i = 0; i < guildsIdList.size(); i++) {
            assertThat(guildIdsInEdges, hasItem(i));
        }


        for (int i = 0; i < kingdomsIdList.size(); i++) {
            assertThat(kingdmsIdsInEdges, hasItem(i));
        }

        assertEquals(TestUtil.findDuplicates(guildIdsInEdges).size(), 0);
    }

    @BeforeClass
    public static void setup() throws Exception {
        configuration = new DataGenConfiguration(CONFIGURATION_FILE_PATH).getInstance();
        logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
    }

}