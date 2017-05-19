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
public class RandomUtil {

    private static final Random rand = new Random();

    private RandomUtil() {
        throw new IllegalAccessError("Utility class");
    }

    public static List<Double> randomGaussianNumbers(double mean, double sd, int numOfNumbers){
        List<Double> randomNumbers = new ArrayList<>();
        for (int idx = 1; idx <= numOfNumbers; ++idx){
            randomNumbers.add(getGaussian(mean, sd));
        }
        return randomNumbers;
    }

    public static Double randomGaussianNumber(double mean, double sd){
        return getGaussian(mean, sd);
    }

    private static double getGaussian(double mean, double sd){
        return mean + rand.nextGaussian() * sd;
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

    /**
     * Returns a random integer uniformly in [a, b].
     *
     * @param  a the left endpoint
     * @param  b the right endpoint
     * @return a random integer uniformly in [a, b]
     */
    public static int randomInt(int a, int b) {
        if ((b <= a) || ((long) b - a >= Integer.MAX_VALUE)) {
            throw new IllegalArgumentException("invalid range: [" + a + ", " + b + "]");
        }
        return rand.nextInt((b + 1) - a) + a;
    }

    /**
     * Returns a random integer uniformly in [a, b).
     *
     * @param  a the left endpoint
     * @param  b the right endpoint
     * @return a random integer uniformly in [a, b)
     * @throws IllegalArgumentException if {@code b <= a}
     * @throws IllegalArgumentException if {@code b - a >= Integer.MAX_VALUE}
     */
    public static int uniform(int a, int b) {
        if ((b <= a) || ((long) b - a >= Integer.MAX_VALUE)) {
            throw new IllegalArgumentException("invalid range: [" + a + ", " + b + "]");
        }
        return a + uniform(b - a);
    }

    /**
     * Returns a random real number uniformly in [a, b).
     *
     * @param  a the left endpoint
     * @param  b the right endpoint
     * @return a random real number uniformly in [a, b)
     * @throws IllegalArgumentException unless {@code a < b}
     */
    public static double uniform(double a, double b) {
        if (!(a < b)) {
            throw new IllegalArgumentException("invalid range: [" + a + ", " + b + "]");
        }
        return a + uniform() * (b-a);
    }

    /**
     * Returns a random integer uniformly in [0, n).
     *
     * @param n number of possible integers
     * @return a random integer uniformly between 0 (inclusive) and {@code n} (exclusive)
     * @throws IllegalArgumentException if {@code n <= 0}
     */
    public static int uniform(int n) {
        if (n <= 0) throw new IllegalArgumentException("argument must be positive");
        return rand.nextInt(n);
    }

    /**
     * Returns a random real number uniformly in [0, 1).
     *
     * @return a random real number uniformly in [0, 1)
     */
    public static double uniform() {
        return rand.nextDouble();
    }

}
