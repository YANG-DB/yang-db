package com.kayhut.fuse.generator;

import com.google.common.collect.LinkedHashMultiset;
import com.kayhut.fuse.generator.helper.TestUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

/**
 * Created by benishue on 22-May-17.
 */
public class DataGeneratorTest {


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

        assertEquals(TestUtil.findDuplicates(personsIdsInEdges).size(), 0);
    }


    @Ignore
    @Test
    public void attachPersonsToGuildsTest() throws Exception {
        List<Integer> personsIdList = IntStream.rangeClosed(0, 9999)
                .boxed().collect(Collectors.toList());

        List<Integer> guildsIdList = IntStream.rangeClosed(0, 99)
                .boxed().collect(Collectors.toList());

        List<Tuple2> personsToGuildsEdges = DataGenerator.attachPersonsToGuilds(guildsIdList, personsIdList);
        //Checking that the size of the EdgeSet is the size of persons population less 0.025% (not belong to any guild)
        assertEquals(9750, personsToGuildsEdges.size());

        List<Integer> personsIdsInEdges = Stream.ofAll(personsToGuildsEdges).map(tuple2 -> (Integer) tuple2._1).toJavaList();
        List<Integer> guildsIdsInEdges = Stream.ofAll(personsToGuildsEdges).map(tuple2 -> (Integer) tuple2._2).toJavaList();

        //Check that all Persons belong to kingdoms
        for (int i = 0; i < 9750; i++) {
            assertThat(personsIdsInEdges, hasItem(i));
        }



        personsToGuildsEdges.stream()
                .collect(Collectors.groupingBy(tuple2 -> tuple2._2, Collectors.counting()))
                .forEach((id, count) -> System.out.println("Guild id = " + id + "\t" + "Count: " + count));

    }

}