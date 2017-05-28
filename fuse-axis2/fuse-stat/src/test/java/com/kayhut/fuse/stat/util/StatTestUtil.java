package com.kayhut.fuse.stat.util;

import org.apache.commons.collections.map.HashedMap;

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
            dragon.put("name", StatTestUtil.generateRandomString(dragonNamePrefixLength) + "_dragon" + i);
            dragon.put("age", randomInt(dragonMinAge, dragonMaxAge));
            dragon.put("color", dragonColors.get(rand.nextInt(dragonColors.size())));
            dragon.put("gender", dragonGenders.get(rand.nextInt(dragonGenders.size())));
            dragon.put("address", StatTestUtil.generateRandomString(dragonAddressLength));

            dragons.add(dragon);
        }
        return dragons;
    }
}
