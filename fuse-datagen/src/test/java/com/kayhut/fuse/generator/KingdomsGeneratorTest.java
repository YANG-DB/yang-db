package com.kayhut.fuse.generator;

import com.google.common.collect.LinkedHashMultiset;
import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.configuration.KingdomConfiguration;
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
public class KingdomsGeneratorTest {

    static final String CONFIGURATION_FILE_PATH = "test.generator.properties";
    static Configuration configuration;
    static Logger logger;

    @Test
    public void generateKingdomsTest() throws Exception {
        List<Kingdom> kingdoms = DataGenerator.generateKingdoms(logger, configuration);
        KingdomConfiguration kingdomConfiguration = new KingdomConfiguration(configuration);
        assertEquals(kingdomConfiguration.getNumberOfNodes(), kingdoms.size());
        assertEquals("0", kingdoms.get(0).getId());
        assertTrue(Arrays.asList(kingdomConfiguration.getKingdoms()).
                contains(RandomUtil.getRandomElementFromList(kingdoms).getName()));
    }

    @Test
    public void attachPersonsToKingdomsTest() throws Exception {
        List<Integer> personsIdList = IntStream.rangeClosed(0, 9999)
                .boxed().collect(Collectors.toList());

        List<Integer> kingdomsIdList = IntStream.rangeClosed(0, 7)
                .boxed().collect(Collectors.toList());

        List<Tuple2> personsToKingdomsEdges = DataGenerator.attachPersonsToKingdoms(kingdomsIdList, personsIdList);
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

        assertEquals(findDuplicates(personsIdsInEdges).size() ,0);
    }

    private Set<Integer> findDuplicates(List<Integer> input) {
        // Linked* preserves insertion order so the returned Sets iteration order is somewhat like the original list
        LinkedHashMultiset<Integer> duplicates = LinkedHashMultiset.create(input);

        // Remove all entries with a count of 1
        duplicates.entrySet().removeIf(entry -> entry.getCount() == 1);

        return duplicates.elementSet();
    }

    @BeforeClass
    public static void setup() throws Exception {
        configuration = new DataGenConfiguration(CONFIGURATION_FILE_PATH).getInstance();
        logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
    }

}