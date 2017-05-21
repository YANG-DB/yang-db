package com.kayhut.fuse.generator.data.generation.entity;

import com.github.javafaker.Name;
import com.kayhut.fuse.generator.configuration.PersonConfiguration;
import com.kayhut.fuse.generator.data.generation.other.PropertiesGenerator;
import com.kayhut.fuse.generator.model.entity.Person;
import com.kayhut.fuse.generator.util.DateUtil;
import com.kayhut.fuse.generator.util.RandomUtil;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class PersonGenerator extends EntityGeneratorBase<PersonConfiguration, Person> {

    public PersonGenerator(PersonConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Person generate() {
        Name fakeName = faker.name();
        Date birthDate = RandomUtil.randomDate(configuration.getStartDateOfStory(), configuration.getEndDateOfStory());
        long lifeExpectancy = Math.round(RandomUtil.randomGaussianNumber(configuration.getLifeExpectancyMean(), configuration.getLifeExpectancySD()));
        Date deathDate = DateUtil.addYearsToDate(birthDate, (int) lifeExpectancy);
        long height = Math.round(RandomUtil.randomGaussianNumber(configuration.getHeightMean(), configuration.getLifeExpectancySD()));

        return Person.PersonBuilder.aPerson()
                .withFirstName(fakeName.firstName())
                .withLastName(fakeName.lastName())
                .withGender(PropertiesGenerator.generateGender())
                .withBirthDate(birthDate)
                .withDeathDate(deathDate)
                .withHeight((int) height)
                .build();
    }
}
