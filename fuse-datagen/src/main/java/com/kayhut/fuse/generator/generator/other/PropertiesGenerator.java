package com.kayhut.fuse.generator.generator.other;

import com.kayhut.fuse.generator.model.enums.Gender;

import java.util.Random;

/**
 * Created by benishue on 15-May-17.
 */
public class PropertiesGenerator {

    private static final Random rand = new Random();

    private PropertiesGenerator() {
        throw new IllegalAccessError("Utility class");
    }

    // 50% Chance for each gender
    public static Gender generateGender() {
        return (rand.nextBoolean() ? Gender.MALE : Gender.FEMALE);
    }


}
