package com.kayhut.fuse.generator;

import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.configuration.PersonConfiguration;
import com.kayhut.fuse.generator.generator.entity.PersonGeneratorBase;
import com.kayhut.fuse.generator.model.entity.Person;
import com.kayhut.fuse.generator.model.enums.Gender;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 15-May-17.
 */
public class PersonGeneratorTest {

    static final String CONFIGURATION_FILE_PATH = "test.generator.properties";
    static Configuration configuration;


    @Test
    public void generatePerson() throws Exception {
        PersonGeneratorBase personGenerator = new PersonGeneratorBase(new PersonConfiguration(configuration));

        for (int i = 0; i < 100; i++) {
            Person personA = personGenerator.generate();
            assertTrue(!personA.getFirstName().isEmpty());
            assertTrue(!personA.getLastName().isEmpty());
            assertTrue(personA.getBirthDate().compareTo(personA.getDeathDate()) < 0);
            assertTrue(personA.getGender() == Gender.FEMALE || personA.getGender() == Gender.MALE);
            System.out.println(personA);
        }
    }

    @BeforeClass
    public static void setup() throws Exception {

        configuration = new DataGenConfiguration(CONFIGURATION_FILE_PATH).getInstance();
    }
}