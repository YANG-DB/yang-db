package com.kayhut.fuse.generator.generator;

import com.kayhut.fuse.generator.model.enums.Gender;

import java.util.Random;

/**
 * Created by benishue on 15-May-17.
 */
public class PropertiesGenerator {

    static final Random rand = new Random();

    // 50% Chance for each gender
    public static Gender generateGender() {
        return (rand.nextBoolean() ? Gender.MALE : Gender.FEMALE);
    }


}
