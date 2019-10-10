package com.yangdb.fuse.generator.util;

/*-
 *
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

    public static List<Double> randomGaussianNumbers(double mean, double sd, int numOfNumbers) {
        List<Double> randomNumbers = new ArrayList<>();
        for (int idx = 1; idx <= numOfNumbers; ++idx) {
            randomNumbers.add(getGaussian(mean, sd));
        }
        return randomNumbers;
    }

    public static Double randomGaussianNumber(double mean, double sd) {
        return getGaussian(mean, sd);
    }

    private static double getGaussian(double mean, double sd) {
        return mean + rand.nextGaussian() * sd;
    }

    public static <T> T enumeratedDistribution(List<Pair<T, Double>> probabilities) {
        EnumeratedDistribution<T> distribution = new EnumeratedDistribution<>(probabilities);
        return distribution.sample();
    }

    public static long randomDateInEpoch(Date startDate, Date endDate) {
        //Date randomDate = new Date(ThreadLocalRandom.nextLong(minDate.getTime(), maxDate.getTime()));
        return ThreadLocalRandom.current().nextLong(startDate.getTime(), endDate.getTime());
    }

    public static Date randomDate(Date startDate, Date endDate) {
        //Date randomDate = new Date(ThreadLocalRandom.nextLong(minDate.getTime(), maxDate.getTime()));
        return (new Date(ThreadLocalRandom.current().nextLong(startDate.getTime(), endDate.getTime())));
    }

    /**
     * @param n num of random numbers
     * @param m normalize sum to m
     * @return n random numbers normalized
     */
    public static double[] getRandDistArray(int n, double m) {
        double randArray[] = new double[n];
        double sum = 0;

        // Generate n random numbers
        for (int i = 0; i < randArray.length; i++) {
            randArray[i] = Math.random();
            sum += randArray[i];
        }

        // Normalize sum to m
        for (int i = 0; i < randArray.length; i++) {
            randArray[i] /= sum;
            randArray[i] *= m;
        }
        return randArray;
    }

    public static double[] getCumulativeDistArray(double[] array) {
        double cumulativeArray[] = new double[array.length];

        for (int i = 0; i < cumulativeArray.length; i++) {
            if (i == 0) {
                cumulativeArray[i] = array[i];
            }
            else {
                cumulativeArray[i] = cumulativeArray[i - 1] + array[i];
            }
        }
        return cumulativeArray;
    }

    /**
     * @param n num of random numbers
     * @param m normalize sum to m
     * @param lambda the rate of the exponential distribution (> 0.0)
     * @return n exp distributed numbers normalized
     */
    public static double[] getExpDistArray(int n, double m, double lambda) {
        double randArray[] = new double[n];
        double sum = 0;

        // Generate n random numbers
        for (int i = 0; i < randArray.length; i++) {
            randArray[i] = exp(lambda);
            sum += randArray[i];
        }

        // Normalize sum to m
        for (int i = 0; i < randArray.length; i++) {
            randArray[i] /= sum;
            randArray[i] *= m;
        }
        return randArray;
    }

    public static <T> T getRandomElementFromList(List<T> list) {
        return list.get(rand.nextInt(list.size()));
    }

    public static <T> T getRandomElementFromArray(T[] array) {
        return array[rand.nextInt(array.length)];
    }

    public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = rand.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    /**
     * Returns a random integer uniformly in [a, b].
     *
     * @param a the left endpoint
     * @param b the right endpoint
     * @return a random integer uniformly in [a, b]
     */
    public static int randomInt(int a, int b) {
        if ((b <= a) || ((long) b - a >= Integer.MAX_VALUE)) {
            throw new IllegalArgumentException(String.format("Invalid range: [%d, %d]", a, b));
        }
        return rand.nextInt((b + 1) - a) + a;
    }

    /**
     * Returns a random integer uniformly in [a, b).
     *
     * @param a the left endpoint
     * @param b the right endpoint
     * @return a random integer uniformly in [a, b)
     * @throws IllegalArgumentException if {@code b <= a}
     * @throws IllegalArgumentException if {@code b - a >= Integer.MAX_VALUE}
     */
    public static int uniform(int a, int b) {
        if ((b <= a) || ((long) b - a >= Integer.MAX_VALUE)) {
            throw new IllegalArgumentException(String.format("Invalid range: [%d, %d]", a, b));
        }
        return a + uniform(b - a);
    }

    /**
     * Returns a random real number uniformly in [a, b).
     *
     * @param a the left endpoint
     * @param b the right endpoint
     * @return a random real number uniformly in [a, b)
     * @throws IllegalArgumentException unless {@code a < b}
     */
    public static double uniform(double a, double b) {
        if (!(a < b)) {
            throw new IllegalArgumentException(String.format("Invalid range: [%s, %s]", a, b));
        }
        return a + uniform() * (b - a);
    }

    /**
     * Returns a random integer uniformly in [0, n).
     *
     * @param n number of possible integers
     * @return a random integer uniformly between 0 (inclusive) and {@code n} (exclusive)
     * @throws IllegalArgumentException if {@code n <= 0}
     */
    public static int uniform(int n) {
        if (n <= 0) throw new IllegalArgumentException("Argument must be positive");
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

    /**
     * Returns a random integer from a geometric distribution with success
     * probability <em>p</em>.
     *
     * @param p the parameter of the geometric distribution
     * @return a random integer from a geometric distribution with success
     * probability {@code p}; or {@code Integer.MAX_VALUE} if
     * {@code p} is (nearly) equal to {@code 1.0}.
     * @throws IllegalArgumentException unless {@code p >= 0.0} and {@code p <= 1.0}
     */
    public static int geometric(double p) {
        if (!(p >= 0.0 && p <= 1.0)) {
            throw new IllegalArgumentException("Probability p must be between 0.0 and 1.0");
        }
        // using algorithm given by Knuth
        return (int) Math.ceil(Math.log(uniform()) / Math.log(1.0 - p));
    }

    /**
     * Returns a random boolean from a Bernoulli distribution with success
     * probability <em>p</em>.
     *
     * @param p the probability of returning {@code true}
     * @return {@code true} with probability {@code p} and
     * {@code false} with probability {@code p}
     * @throws IllegalArgumentException unless {@code 0} &le; {@code p} &le; {@code 1.0}
     */
    public static boolean bernoulli(double p) {
        if (!(p >= 0.0 && p <= 1.0))
            throw new IllegalArgumentException("Probability p must be between 0.0 and 1.0");
        return uniform() < p;
    }

    /**
     * Returns a random boolean from a Bernoulli distribution with success
     * probability 1/2.
     *
     * @return {@code true} with probability 1/2 and
     * {@code false} with probability 1/2
     */
    public static boolean bernoulli() {
        return bernoulli(0.5);
    }

    /**
     * Returns a random integer from a Poisson distribution with mean &lambda;.
     *
     * @param lambda the mean of the Poisson distribution
     * @return a random integer from a Poisson distribution with mean {@code lambda}
     * @throws IllegalArgumentException unless {@code lambda > 0.0} and not infinite
     */
    public static int poisson(double lambda) {
        if (!(lambda > 0.0))
            throw new IllegalArgumentException("Lambda must be positive");
        if (Double.isInfinite(lambda))
            throw new IllegalArgumentException("Lambda must not be infinite");
        // using algorithm given by Knuth
        // see http://en.wikipedia.org/wiki/Poisson_distribution
        int k = 0;
        double p = 1.0;
        double expLambda = Math.exp(-lambda);
        do {
            k++;
            p *= uniform();
        } while (p >= expLambda);
        return k - 1;
    }

    /**
     * Returns a random real number from an exponential distribution
     * with rate &lambda;.
     *
     * @param  lambda the rate of the exponential distribution
     * @return a random real number from an exponential distribution with
     *         rate {@code lambda}
     * @throws IllegalArgumentException unless {@code lambda > 0.0}
     */
    public static double exp(double lambda) {
        if (!(lambda > 0.0))
            throw new IllegalArgumentException("Lambda must be positive");
        return -Math.log(1 - uniform()) / lambda;
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param  a the array to shuffle
     * @throws IllegalArgumentException if {@code a} is {@code null}
     */
    public static void shuffle(double[] a) {
        if (a == null) throw new IllegalArgumentException("Argument array is null");
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int r = i + uniform(n-i);     // between i and n-1
            double temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param  a the array to shuffle
     * @throws IllegalArgumentException if {@code a} is {@code null}
     */
    public static void shuffle(int[] a) {
        if (a == null) throw new IllegalArgumentException("Argument array is null");
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int r = i + uniform(n-i);     // between i and n-1
            int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

}
