package com.yangdb.fuse.generator;

import com.yangdb.fuse.generator.configuration.DataGenConfiguration;
import com.yangdb.fuse.generator.configuration.PersonConfiguration;
import com.yangdb.fuse.generator.data.generation.PersonsGraphGenerator;
import com.yangdb.fuse.generator.data.generation.entity.PersonGenerator;
import com.yangdb.fuse.generator.helper.TestUtil;
import com.yangdb.fuse.generator.model.entity.Person;
import com.yangdb.fuse.generator.model.enums.Gender;
import com.yangdb.fuse.generator.model.enums.RelationType;
import com.yangdb.fuse.generator.util.CsvUtil;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 15-May-17.
 */
public class PersonsGeneratorTest {

    static String CONFIGURATION_SMALL_FILE_PATH = "test.generator.properties";
    static String CONFIGURATION_MASSIVE_FILE_PATH = "test.generator.massive.properties";

    private final static int NUM_OF_PERSONS = 1000;
    private final static int NUM_OF_DRAGONS = 10000;
    private final static int NUM_OF_HORSES = 6500;

    static Configuration configuration;
    static PersonConfiguration personConfiguration;
    static String personsFilePath;
    static String personsKnowsRelationFilePath;
    static Logger logger;


    @Test
    public void personsDataGenerationMassiveGraphTest() throws Exception {
        loadConfigurations(CONFIGURATION_MASSIVE_FILE_PATH);
        File personsFile = new File(personsFilePath);
        Files.deleteIfExists(personsFile.toPath());
        PersonsGraphGenerator personsGraphGenerator = new PersonsGraphGenerator(personConfiguration);
        personsGraphGenerator.generatePersonsGraph();
        assertTrue(TestUtil.isFileExists(personsFilePath));
        List<String[]> dragonsLines = CsvUtil.readCSV(personsFilePath, ',');
        assertTrue(dragonsLines.size() > personConfiguration.getNumberOfNodes() - 1);
        assertTrue(dragonsLines.get(1)[0].equals("0")); //[0] = graph Id
        assertTrue(dragonsLines.get(2)[0].equals("1")); // No weird 'Jumps'
    }

    @Test
    public void generatePersonTest() throws Exception {
        PersonGenerator personGenerator = new PersonGenerator(new PersonConfiguration(configuration));

        for (int i = 0; i < 100; i++) {
            Person personA = personGenerator.generate();
            personA.setId(Integer.toString(i));
            assertTrue(!personA.getFirstName().isEmpty());
            assertTrue(!personA.getLastName().isEmpty());
            assertTrue(personA.getBirthDate().compareTo(personA.getDeathDate()) < 0);
            assertTrue(personA.getGender() == Gender.FEMALE || personA.getGender() == Gender.MALE);
            //System.out.println(personA);
        }
    }

    @Test
    public void attachDragonsToPersonsTest() throws Exception {
        List<String> personsIdList = IntStream.rangeClosed(0, NUM_OF_PERSONS - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        List<String> dragonsIdList = IntStream.rangeClosed(0, NUM_OF_DRAGONS - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        //Usually we have more animals than persons
        double meanDragonsPerPerson = (dragonsIdList.size() / (double) personsIdList.size());
        double sdDragonsPerPerson = 2;

        PersonsGraphGenerator personsGraphGenerator = new PersonsGraphGenerator(new PersonConfiguration(configuration));
        Map<String, List<String>> dragonsToPersonsSet = personsGraphGenerator.attachDragonsToPerson(
                dragonsIdList,
                personsIdList,
                meanDragonsPerPerson,
                sdDragonsPerPerson);

        int sum = 0;
        int count = 0;
        List<String> allDragons = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : dragonsToPersonsSet.entrySet()) {
            //check that we dond have duplicates of dragons per person
            List<String> assignedDragons = entry.getValue();
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
        Set<String> dupDragons = TestUtil.findDuplicates(allDragons);


        dupDragons.forEach(dupDragonId -> {
            for (Map.Entry<String, List<String>> entry : dragonsToPersonsSet.entrySet()) {
                //check that we dond have duplicates of dragons per person
                List<String> dragonsList = entry.getValue();
                String personId = entry.getKey();
                if (dragonsList.contains(dupDragonId)) {
                    System.out.println("Person id: " + personId + ", Dragon Id: " + dupDragonId);
                }
            }
        });


//        dupDragons.forEach(dragonId -> System.out.println(dragonId));
        assertFalse(TestUtil.hasDuplicate(allDragons));

    }

    @Test
    public void attachHorsesToPersonsTest() throws Exception {
        List<String> personsIdList = IntStream.rangeClosed(0, NUM_OF_PERSONS - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());

        List<String> horsesIdList = IntStream.rangeClosed(0, NUM_OF_HORSES - 1)
                .mapToObj(Integer::toString).collect(Collectors.toList());


        //Usually we have more animals than persons
        double meanHorsesPerPerson = (horsesIdList.size() / (double) personsIdList.size());
        double sdHorsesPerPerson = 2;



        PersonsGraphGenerator personsGraphGenerator = new PersonsGraphGenerator(new PersonConfiguration(configuration));

        Map<String, List<String>> horsesToPersonsSet = personsGraphGenerator.attachHorsesToPerson(horsesIdList,
                personsIdList,
                meanHorsesPerPerson,
                sdHorsesPerPerson);


        int sum = 0;
        int count = 0;
        List<String> allHorses = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : horsesToPersonsSet.entrySet()) {
            //check that we dond have duplicates of dragons per person
            List<String> assignedHorses = entry.getValue();
            allHorses.addAll(assignedHorses);
            assertFalse(TestUtil.hasDuplicate(assignedHorses));
            sum += assignedHorses.size();
            count++;
        }
        double avgHorsesPerPerson = sum / (double) count;
        assertEquals(meanHorsesPerPerson, avgHorsesPerPerson, sdHorsesPerPerson);
//        System.out.println(sum / (double) count);

        //We should have less assigned dragons to persons.
        assertTrue(allHorses.size() <= NUM_OF_DRAGONS);
//        System.out.println(allHorses.size());
//        System.out.println(horsesToPersonsSet.size());

        //We should not have duplicate dragons assigned simultaneously to several persons
        Set<String> dupHorses = TestUtil.findDuplicates(allHorses);


        dupHorses.forEach(dupHorseId -> {
            for (Map.Entry<String, List<String>> entry : horsesToPersonsSet.entrySet()) {
                //check that we dond have duplicates of dragons per person
                List<String> assignedHorses = entry.getValue();
                String personId = entry.getKey();
                if (assignedHorses.contains(dupHorseId)) {
                    System.out.println("Person id: " + personId + ", Horse Id: " + dupHorseId);
                }
            }
        });


//        dupHorses.forEach(dragonId -> System.out.println(dragonId));
        assertFalse(TestUtil.hasDuplicate(allHorses));

    }


    @BeforeClass
    public static void setUp() throws Exception {
        logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
        loadConfigurations(CONFIGURATION_MASSIVE_FILE_PATH);
    }

    private static void loadConfigurations(String configurationFilePath) {
        configuration = new DataGenConfiguration(configurationFilePath).getInstance();
        personConfiguration = new PersonConfiguration(configuration);
        personsFilePath = personConfiguration.getEntitiesFilePath();
        personsKnowsRelationFilePath = personConfiguration.getRelationsFilePath().replace(".csv","") + "_" + RelationType.KNOWS + ".csv";
    }
}