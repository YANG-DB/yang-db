package com.kayhut.fuse.stat;

import org.apache.commons.collections.map.HashedMap;

import java.util.*;

/**
 * Created by benishue on 24-May-17.
 */
public class StatTestUtil {

    public static String generateRandomString(int stringSize){
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < stringSize; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    public static Iterable<Map<String, Object>> createDragons(int numDragons) {
        Random r = new Random();
        List<String> colors = Arrays.asList("red", "green", "yellow", "blue", "00" ,"11" ,"22" ,"33" ,"44" ,"55");
        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashedMap();
            dragon.put("id", Integer.toString(i));
            dragon.put("name", StatTestUtil.generateRandomString(10) + " dragon" + i);
            dragon.put("age", r.nextInt(100));
            dragon.put("color", colors.get(r.nextInt(colors.size())));
            dragon.put("gender", (r.nextBoolean() ? "MALE" : "FEMALE"));
            dragon.put("address", StatTestUtil.generateRandomString(20));

            dragons.add(dragon);
        }
        return dragons;
    }
}
