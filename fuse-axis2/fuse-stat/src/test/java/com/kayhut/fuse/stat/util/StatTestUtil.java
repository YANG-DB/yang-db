package com.kayhut.fuse.stat.util;

import org.apache.commons.collections.map.HashedMap;

import java.text.ParseException;
import java.util.*;

/**
 * Created by benishue on 24-May-17.
 */
public class StatTestUtil {

    private static final Random rand = new Random();

    public static String generateRandomString(int stringSize) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < stringSize; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * @param a
     * @param b
     * @return random number between [a, b]
     */
    public static int randomInt(int a, int b) {
        if ((b <= a) || ((long) b - a >= Integer.MAX_VALUE)) {
            throw new IllegalArgumentException(String.format("Invalid range: [%d, %d]", a, b));
        }
        return rand.nextInt((b + 1) - a) + a;
    }

    /**
     * @param a
     * @param b
     * @return random number between [a, b]
     */
    public static long randomLong(long a, long b) {
        if ((b <= a)) {
            throw new IllegalArgumentException(String.format("Invalid range: [%d, %d]", a, b));
        }
        return (long) (rand.nextDouble() * ((b + 1) - a)) + a;
    }

    /**
     * @param numDragons
     * @param dragonMinAge
     * @param dragonMaxAge
     * @param dragonNamePrefixLength
     * @param dragonColors
     * @param dragonGenders
     * @param dragonAddressLength
     * @return Dragon documents with sequential id [0..numOfDragons)
     */
    public static Iterable<Map<String, Object>> createDragons(int numDragons,
                                                              int dragonMinAge,
                                                              int dragonMaxAge,
                                                              int dragonNamePrefixLength,
                                                              List<String> dragonColors,
                                                              List<String> dragonGenders,
                                                              int dragonAddressLength
    ) {
        List<Map<String, Object>> dragons = new ArrayList<>();
        for (int i = 0; i < numDragons; i++) {
            Map<String, Object> dragon = new HashedMap();
            dragon.put("id", Integer.toString(i));
            dragon.put("name", generateRandomString(dragonNamePrefixLength) + "_dragon" + i);
            dragon.put("age", randomInt(dragonMinAge, dragonMaxAge));
            dragon.put("color", dragonColors.get(rand.nextInt(dragonColors.size())));
            dragon.put("gender", dragonGenders.get(rand.nextInt(dragonGenders.size())));
            dragon.put("address", generateRandomString(dragonAddressLength));

            dragons.add(dragon);
        }
        return dragons;
    }

    public static Iterable<Map<String, Object>> createDragonFireDragonEdges(int numDragons,
                                                                            long startDate,
                                                                            long endDate,
                                                                            int minTemp,
                                                                            int maxTemp) throws ParseException {
        List<Map<String, Object>> fireEdges = new ArrayList<>();

        int counter = 0;
        for (int i = 0; i < numDragons; i++) {
            for (int j = 0; j < i; j++) {
                Map<String, Object> fireEdge = new HashMap<>();
                fireEdge.put("id", "fire_" + counter);
                fireEdge.put("time", randomLong(startDate, endDate));
                fireEdge.put("direction", "OUT");
                fireEdge.put("temperature", randomInt(minTemp, maxTemp));

                Map<String, Object> fireEdgeDual = new HashMap<>();
                fireEdgeDual.put("id", "fire_" + +counter + 1);
                fireEdgeDual.put("time", randomLong(startDate, endDate));
                fireEdgeDual.put("direction", "IN");

                Map<String, Object> entityAI = new HashMap<>();
                entityAI.put("id", "Dragon_" + i);
                entityAI.put("type", "Dragon");
                Map<String, Object> entityAJ = new HashMap<>();
                entityAJ.put("id", "Dragon_" + j);
                entityAJ.put("type", "Dragon");
                Map<String, Object> entityBI = new HashMap<>();
                entityBI.put("id", "Dragon_" + i);
                entityBI.put("type", "Dragon");
                Map<String, Object> entityBJ = new HashMap<>();
                entityBJ.put("id", "Dragon_" + j);
                entityBJ.put("type", "Dragon");

                fireEdge.put("entityA", entityAI);
                fireEdge.put("entityB", entityBJ);
                fireEdgeDual.put("entityA", entityAJ);
                fireEdgeDual.put("entityB", entityBI);

                fireEdges.addAll(Arrays.asList(fireEdge, fireEdgeDual));

                counter += 2;
            }
        }

        return fireEdges;
    }




}
