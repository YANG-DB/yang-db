package com.kayhut.fuse.epb.util;

import com.kayhut.fuse.stat.model.bucket.Bucket;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.result.StatRangeResult;
import com.kayhut.fuse.stat.model.result.StatResultBase;
import com.kayhut.fuse.stat.model.result.StatTermResult;
import com.kayhut.fuse.stat.util.StatUtil;
import org.apache.commons.collections.map.HashedMap;

import java.util.*;

/**
 * Created by benishue on 25-May-17.
 */
public class EpbUtil {
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


    public static Iterable<Map<String, Object>> createStatistics(int numDragons) {
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

    public static Iterable<Map<String, Object>> createNumericStatistics(String index,
                                                                        String type,
                                                                        String field,
                                                                        long min,
                                                                        long max,
                                                                        int numOfBins) {
        List<BucketRange<Double>> numericBuckets = StatUtil.createNumericBuckets(min, max, Math.toIntExact(numOfBins));
        List<StatRangeResult> statRangeResults = new ArrayList<>();
        for (int i = 0; i < numericBuckets.size(); i++) {
            for (BucketRange<Double> bucketRange : numericBuckets) {
                int j = 0;
                StatRangeResult<Double> statRangeResult = new StatRangeResult<>
                        (index, type, field, Integer.toString(j), DataType.numeric, bucketRange.getStart(), bucketRange.getEnd(), j, j);
                statRangeResults.add(statRangeResult);
                j++;
            }
        }
        return StatUtil.prepareStatDocs(statRangeResults);
    }


}
