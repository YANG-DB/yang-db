package com.kayhut.fuse.epb.util;

import com.kayhut.fuse.stat.model.bucket.Bucket;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.result.StatRangeResult;
import com.kayhut.fuse.stat.model.result.StatResultBase;
import com.kayhut.fuse.stat.model.result.StatTermResult;
import com.kayhut.fuse.stat.util.StatUtil;
import javaslang.Tuple2;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;

import java.util.*;

/**
 * Created by benishue on 25-May-17.
 */
public class EpbTestUtil {
    public static Iterable<Map<String, Object>> createDragons(int numDragons) {
        Random r = new Random();
        List<String> colors = Arrays.asList("red", "green", "yellow", "blue");
        List<Map<String, Object>> dragons = new ArrayList<>();
        for (int i = 0; i < numDragons; i++) {
            Map<String, Object> dragon = new HashedMap();
            dragon.put("id", Integer.toString(i));
            dragon.put("name", generateRandomString(10) + " dragon" + i);
            dragon.put("age", r.nextInt(100));
            dragon.put("color", colors.get(r.nextInt(colors.size())));
            dragon.put("gender", (r.nextBoolean() ? "MALE" : "FEMALE"));
            dragon.put("address", generateRandomString(20));

            dragons.add(dragon);
        }
        return dragons;
    }


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



}
