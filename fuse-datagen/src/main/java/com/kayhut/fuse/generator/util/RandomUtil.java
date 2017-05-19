package com.kayhut.fuse.generator.util;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by benishue on 16-May-17.
 */
public class RandomGenerator {

    private static final Random rand = new Random();

    private RandomGenerator() {
        throw new IllegalAccessError("Utility class");
    }

    public static List<Double> randomGaussianNumbers(double mean, double variance, int numOfNumbers){
        List<Double> randomNumbers = new ArrayList<>();
        for (int idx = 1; idx <= numOfNumbers; ++idx){
            randomNumbers.add(getGaussian(mean, variance));
        }
        return randomNumbers;
    }

    private static double getGaussian(double aMean, double aVariance){
        return aMean + rand.nextGaussian() * aVariance;
    }

    public static <T> T enumeratedDistribution(List<Pair<T, Double>> probabilities){
        EnumeratedDistribution<T> distribution = new EnumeratedDistribution<>(probabilities);
        return distribution.sample();
    }

    public static long randomDateInEpoch(Date startDate, Date endDate){
        //Date randomDate = new Date(ThreadLocalRandom.nextLong(minDate.getTime(), maxDate.getTime()));
        return ThreadLocalRandom.current().nextLong(startDate.getTime(), endDate.getTime());
    }

    public static Date randomDate(Date startDate, Date endDate){
        //Date randomDate = new Date(ThreadLocalRandom.nextLong(minDate.getTime(), maxDate.getTime()));
        return (new Date(ThreadLocalRandom.current().nextLong(startDate.getTime(), endDate.getTime())));
    }

    public static int randomInt(int min, int max) {
        return rand.nextInt((max + 1) - min) + min;
    }

}
