package com.kayhut.fuse.generator;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.kayhut.fuse.generator.helper.TestUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

/**
 * Created by benishue on 22-May-17.
 */
public class DataGeneratorTest {

    static final String CONFIGURATION_FILE_PATH = "test.generator.properties";
    private final static int NUM_OF_PERSONS = 1000;
    private final static int NUM_OF_KINGDOMS = 8;
    private final static int NUM_OF_GUILDS = 100;
    private final static int NUM_OF_DRAGONS = 10000;
    private final static int NUM_OF_HORSES = 6500;



    @Test
    public void attachPersonsToKingdomsTest() throws Exception {
        List<Integer> personsIdList = IntStream.rangeClosed(0, NUM_OF_PERSONS - 1)
                .boxed().collect(Collectors.toList());

        List<Integer> kingdomsIdList = IntStream.rangeClosed(0, NUM_OF_KINGDOMS - 1)
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

    @Test
    public void attachPersonsToGuildsTest() throws Exception {
        List<Integer> personsIdList = IntStream.rangeClosed(0, NUM_OF_PERSONS - 1)
                .boxed().collect(Collectors.toList());

        List<Integer> guildsIdList = IntStream.rangeClosed(0, NUM_OF_GUILDS - 1)
                .boxed().collect(Collectors.toList());

        Map<Integer, List<Integer>> personsToGuildsEdges = DataGenerator.attachPersonsToGuilds(guildsIdList, personsIdList);


        //Check there are no duplicates
        for (Map.Entry<Integer, List<Integer>> entry : personsToGuildsEdges.entrySet()) {
            List<Integer> listOfMembers = entry.getValue();
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

    @Test
    public void attachDragonsToPersonsTest() throws Exception {
        List<Integer> personsIdList = IntStream.rangeClosed(0, NUM_OF_PERSONS - 1)
                .boxed().collect(Collectors.toList());

        List<Integer> dragonsIdList = IntStream.rangeClosed(0, NUM_OF_DRAGONS - 1)
                .boxed().collect(Collectors.toList());

        //Usually we have more animals than persons
        double meanDragonsPerPerson = (dragonsIdList.size() / (double) personsIdList.size());
        double sdDragonsPerPerson = 2;

        Map<Integer, List<Integer>> dragonsToPersonsSet = DataGenerator.attachDragonsToPersons(
                dragonsIdList,
                personsIdList,
                meanDragonsPerPerson,
                sdDragonsPerPerson);

        int sum = 0;
        int count = 0;
        List<Integer> allDragons = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : dragonsToPersonsSet.entrySet()) {
            //check that we dond have duplicates of dragons per person
            List<Integer> assignedDragons = entry.getValue();
            allDragons.addAll(assignedDragons);
            assertFalse(TestUtil.hasDuplicate(assignedDragons));
            sum += assignedDragons.size();
            count++;
        }
        double avgDragonsPerPerson = sum / (double) count;
        assertEquals(meanDragonsPerPerson, avgDragonsPerPerson, sdDragonsPerPerson);
        //System.out.println(sum / (double) count);

        //We should have less assigned dragons to persons.
        assertTrue(allDragons.size() <= NUM_OF_DRAGONS);
        //System.out.println(allDragons.size());
        //System.out.println(dragonsToPersonsSet.size());

        //We should not have duplicate dragons assigned simultaneously to several persons
        Set<Integer> dupDragons = TestUtil.findDuplicates(allDragons);


        dupDragons.forEach(dupDragonId -> {
            for (Map.Entry<Integer, List<Integer>> entry : dragonsToPersonsSet.entrySet()) {
                //check that we dond have duplicates of dragons per person
                List<Integer> dragonsList = entry.getValue();
                Integer personId = entry.getKey();
                if (dragonsList.contains(dupDragonId)) {
                    System.out.println("Person id: " + personId + ", Dragon Id: " + dupDragonId);
                }
            }
        });


        dupDragons.forEach(dragonId -> System.out.println(dragonId));
        assertFalse(TestUtil.hasDuplicate(allDragons));

    }

    @Test
    public void attachHorsesToPersonsTest() throws Exception {
        List<Integer> personsIdList = IntStream.rangeClosed(0, NUM_OF_PERSONS - 1)
                .boxed().collect(Collectors.toList());

        List<Integer> horsesIdList = IntStream.rangeClosed(0, NUM_OF_HORSES - 1)
                .boxed().collect(Collectors.toList());


        //Usually we have more animals than persons
        double meanHorsesPerPerson = (horsesIdList.size() / (double) personsIdList.size());
        double sdHorsesPerPerson = 2;

        Map<Integer, List<Integer>> horsesToPersonsSet = DataGenerator.attachHorsesToPersons(horsesIdList,
                personsIdList,
                meanHorsesPerPerson,
                sdHorsesPerPerson);


        int sum = 0;
        int count = 0;
        List<Integer> allHorses = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : horsesToPersonsSet.entrySet()) {
            //check that we dond have duplicates of dragons per person
            List<Integer> assignedHorses = entry.getValue();
            allHorses.addAll(assignedHorses);
            assertFalse(TestUtil.hasDuplicate(assignedHorses));
            sum += assignedHorses.size();
            count++;
        }
        double avgHorsesPerPerson = sum / (double) count;
        assertEquals(meanHorsesPerPerson, avgHorsesPerPerson, sdHorsesPerPerson);
        System.out.println(sum / (double) count);

        //We should have less assigned dragons to persons.
        assertTrue(allHorses.size() <= NUM_OF_DRAGONS);
        System.out.println(allHorses.size());
        System.out.println(horsesToPersonsSet.size());

        //We should not have duplicate dragons assigned simultaneously to several persons
        Set<Integer> dupHorses = TestUtil.findDuplicates(allHorses);


        dupHorses.forEach(dupHorseId -> {
            for (Map.Entry<Integer, List<Integer>> entry : horsesToPersonsSet.entrySet()) {
                //check that we dond have duplicates of dragons per person
                List<Integer> assignedHorses = entry.getValue();
                Integer personId = entry.getKey();
                if (assignedHorses.contains(dupHorseId)) {
                    System.out.println("Person id: " + personId + ", Horse Id: " + dupHorseId);
                }
            }
        });


        dupHorses.forEach(dragonId -> System.out.println(dragonId));
        assertFalse(TestUtil.hasDuplicate(allHorses));

    }

    @Test
    public void attachHorsesToKingdomsTest() throws Exception {
        List<Integer> horsesIdList = IntStream.rangeClosed(0, NUM_OF_HORSES - 1)
                .boxed().collect(Collectors.toList());

        List<Integer> kingdomsIdList = IntStream.rangeClosed(0, NUM_OF_KINGDOMS - 1)
                .boxed().collect(Collectors.toList());

        List<Tuple2> horsesToKingdomsEdges = DataGenerator.attachHorseToKingdom(kingdomsIdList, horsesIdList);
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
        List<Integer> guildsIdList = IntStream.rangeClosed(0, NUM_OF_GUILDS - 1)
                .boxed().collect(Collectors.toList());

        List<Integer> kingdomsIdList = IntStream.rangeClosed(0, NUM_OF_KINGDOMS - 1)
                .boxed().collect(Collectors.toList());

        List<Tuple2> guildsToKingdomsEdges = DataGenerator.attachGuildToKingdom(kingdomsIdList, guildsIdList);
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
    public static void setUp() throws Exception {
        DataGenerator.loadConfiguration(CONFIGURATION_FILE_PATH);

    }
}