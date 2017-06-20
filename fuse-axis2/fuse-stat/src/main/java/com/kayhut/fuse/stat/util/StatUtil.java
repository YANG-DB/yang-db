package com.kayhut.fuse.stat.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.StatCalculator;
import com.kayhut.fuse.stat.model.configuration.*;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.enums.HistogramType;
import com.kayhut.fuse.stat.model.result.StatGlobalCardinalityResult;
import com.kayhut.fuse.stat.model.result.StatRangeResult;
import com.kayhut.fuse.stat.model.result.StatResultBase;
import com.kayhut.fuse.stat.model.result.StatTermResult;
import javaslang.collection.Stream;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by benishue on 30-Apr-17.
 */
public class StatUtil {

    private static final Logger logger = LoggerFactory.getLogger(StatUtil.class);

    private StatUtil() {
        throw new IllegalAccessError("Utility class");
    }

    //region Public Methods
    public static String readJsonToString(String jsonRelativePath) {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(jsonRelativePath)));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return contents;
    }

    public static Optional<StatContainer> getStatConfigurationObject(Configuration configuration) {
        Optional<StatContainer> resultObj = Optional.empty();
        try {
            String statConfigurationFilePath = configuration.getString("statistics.configuration.file");
            String statJson = readJsonToString(statConfigurationFilePath);
            //logger.info("Statistics Configuration: \n {}", statJson);
            resultObj = Optional.of(new ObjectMapper().readValue(statJson, StatContainer.class));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return resultObj;
    }

    public static Optional<Type> getTypeConfiguration(StatContainer statContainer, String typeName) {
        Optional<Type> typeElement = Optional.empty();
        List<Type> types = Stream.ofAll(statContainer.getTypes()).filter(type -> type.getType().equals(typeName)).toJavaList();
        if (!types.isEmpty())
            typeElement = Optional.ofNullable(types.get(0));
        return typeElement;
    }

    public static Optional<Field> geFieldConfiguration(StatContainer statContainer, String typeName, String fieldName) {
        Optional<Field> fieldElement = Optional.empty();
        Optional<Type> typeElement = getTypeConfiguration(statContainer, typeName);
        if (typeElement.isPresent()) {
            List<Field> fields = Stream.ofAll(typeElement.get().getFields()).filter(field -> field.getField().equals(fieldName)).toJavaList();
            if (!fields.isEmpty()) {
                fieldElement = Optional.ofNullable(fields.get(0));
            }
        }
        return fieldElement;
    }

    public static Optional<List<Field>> getFieldsWithHistogram(StatContainer statContainer, String typeName, HistogramType histogramType) {
        Optional<List<Field>> fields = Optional.empty();
        Optional<Type> typeElement = getTypeConfiguration(statContainer, typeName);
        if (typeElement.isPresent()) {
            fields = Optional.ofNullable(Stream.ofAll(typeElement.get().getFields()).filter(field -> field.getHistogram().getHistogramType() == histogramType).toJavaList());
        }
        return fields;
    }

    public static Optional<List<Field>> getFieldsWithNumericHistogram(StatContainer statContainer, String typeName) {
        return getFieldsWithHistogram(statContainer, typeName, HistogramType.numeric);
    }

    public static Optional<List<Field>> getFieldsWithStringHistogram(StatContainer statContainer, String typeName) {
        return getFieldsWithHistogram(statContainer, typeName, HistogramType.string);
    }

    public static Optional<List<Field>> getFieldsWithCompositeHistogram(StatContainer statContainer, String typeName) {
        return getFieldsWithHistogram(statContainer, typeName, HistogramType.composite);
    }

    public static Optional<List<Field>> getFieldsWithManualHistogram(StatContainer statContainer, String typeName) {
        return getFieldsWithHistogram(statContainer, typeName, HistogramType.manual);
    }

    public static Optional<List<Field>> getFieldsWithTermHistogram(StatContainer statContainer, String typeName) {
        return getFieldsWithHistogram(statContainer, typeName, HistogramType.term);
    }

    public static Optional<List<Field>> getFieldsWithDynamicHistogram(StatContainer statContainer, String typeName) {
        return getFieldsWithHistogram(statContainer, typeName, HistogramType.dynamic);
    }

    public static Iterable<Map<String, Object>> prepareStatDocs(List<? extends StatResultBase> bucketStatResults) {
        List<Map<String, Object>> buckets = new ArrayList<>();
        for (StatResultBase bucketStatResult : bucketStatResults) {
            Map<String, Object> bucket = new HashedMap();
            //Deafualt fields for all statistics documents
            bucket.put("index", bucketStatResult.getIndex());
            bucket.put("type", bucketStatResult.getType());
            bucket.put("field", bucketStatResult.getField());

            //Statics Document for range - we are intrested in knowing if its a string range or a numeric range
            // In order to create separate statistics fields per each
            if (bucketStatResult instanceof StatRangeResult) {
                String bucketId = createRangeBucketUniqueId(bucketStatResult.getIndex(),
                        bucketStatResult.getType(),
                        bucketStatResult.getField(),
                        ((StatRangeResult) bucketStatResult).getLowerBound(),
                        ((StatRangeResult) bucketStatResult).getUpperBound());
                bucket.put("id", bucketId);
                if (bucketStatResult.getDataType() == DataType.numeric ||
                        bucketStatResult.getDataType() == DataType.string) {
                    bucket.put("upper_bound_" + bucketStatResult.getDataType(), ((StatRangeResult) bucketStatResult).getUpperBound());
                    bucket.put("lower_bound_" + bucketStatResult.getDataType(), ((StatRangeResult) bucketStatResult).getLowerBound());
                }
            }

            //Statistics Document for term (we do not care about bounds)
            if (bucketStatResult instanceof StatTermResult) {
                String bucketId = createTermBucketUniqueId(bucketStatResult.getIndex(),
                        bucketStatResult.getType(),
                        bucketStatResult.getField(),
                        ((StatTermResult) bucketStatResult).getTerm());
                bucket.put("id", bucketId);
                bucket.put("term", ((StatTermResult) bucketStatResult).getTerm());
            }

            //Global Cardinality Statistic document
            if (bucketStatResult instanceof StatGlobalCardinalityResult) {
                String bucketId = createGlobalBucketUniqueId(bucketStatResult.getIndex(),
                        bucketStatResult.getType(),
                        bucketStatResult.getField(),
                        ((StatGlobalCardinalityResult) bucketStatResult).getDirection());
                bucket.put("id", bucketId);
                bucket.put("direction", ((StatGlobalCardinalityResult) bucketStatResult).getDirection());
            }

            bucket.put("count", bucketStatResult.getDocCount());
            bucket.put("cardinality", bucketStatResult.getCardinality());
            buckets.add(bucket);
        }
        return buckets;
    }

    public static String createRangeBucketUniqueId(String indexName, String typeName, String fieldName, Object lowerBound, Object upperBound) {
        return hashString(indexName + typeName + fieldName + lowerBound + upperBound);
    }

    public static String createTermBucketUniqueId(String indexName, String typeName, String fieldName, Object term) {
        return hashString(indexName + typeName + fieldName + term);
    }

    public static String createGlobalBucketUniqueId(String indexName, String typeName, String fieldName, String direction) {
        return hashString(indexName + typeName + fieldName + direction);
    }

    public static Optional<Field> getFieldByName(StatContainer statContainer, String typeName, String fieldName) {
        Optional<Field> field = Optional.empty();
        Optional<Type> typeElement = getTypeConfiguration(statContainer, typeName);
        if (typeElement.isPresent()) {
            field = typeElement.get().getFields().stream().filter(f -> f.getField().equals(fieldName)).findFirst();
        }
        return field;
    }

    public static List<BucketRange<String>> calculateAlphabeticBuckets(int startCode, int numChars, int prefixLen, int interval) {
        List<BucketRange<String>> buckets = new ArrayList<>();

        int numOfBuckets = (int) Math.ceil(Math.pow(numChars, prefixLen) / interval);
        for (int i = 0; i < numOfBuckets; i++) {
            int bucketIdx = i * interval;
            buckets.add(new BucketRange(calcBucketStart(numChars, startCode, prefixLen, bucketIdx),
                    calcBucketEnd(numChars, startCode, prefixLen, bucketIdx, interval)));
        }

        //Fix last bucket:
        //  if the number of combinations is not perfectly divided by the interval, the end of the last bucket will loop back to earlier values.
        if (numOfBuckets > Math.pow(numChars, prefixLen) / interval) {
            char[] end = new char[prefixLen];
            for (int i = 0; i < prefixLen; i++) {
                end[i] = Character.toChars(startCode + numChars - 1)[0];
            }
            buckets.get(buckets.size() - 1).setEnd(String.valueOf(end));
        }

        return buckets;
    }

    /**
     * Create a MD5 hash of a given message. Used for creating unique document IDs.
     *
     * @param message String to hash
     * @return Hash string
     */
    public static String hashString(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bucketDescriptionBytes = message.getBytes("UTF8");
            byte[] bucketHash = digest.digest(bucketDescriptionBytes);

            return org.elasticsearch.common.Base64.encodeBytes(bucketHash, org.elasticsearch.common.Base64.URL_SAFE).replaceAll("\\s", "");

        } catch (NoSuchAlgorithmException e) {

            logger.error("Could not hash the message: {}", message);
            logger.error("The hash algorithm used is not supported. Stack trace follows.", e);

            return null;
        } catch (UnsupportedEncodingException e) {

            logger.error("Could not hash the message: {}", message);
            logger.error("The character encoding used is not supported. Stack trace follows.", e);

            return null;
        } catch (IOException e) {

            logger.error("Could not hash the message: {}", message);
            logger.error("A problem occurred when encoding as URL safe hash. Stack trace follows.", e);

            return null;
        }
    }

    /**
     * @param min       lowest value
     * @param max       highest value
     * @param numOfBins number of bins. Must be strictly positive.
     * @return a list of buckets of evenly distributed bins between the minimum and maximum values
     */
    public static List<BucketRange<Double>> createNumericBuckets(double min, double max, int numOfBins) {
        double[] bucketsData = new double[numOfBins + 1];
        for (int i = 0; i <= numOfBins; i++) {
            bucketsData[i] = min + i * (max - min) / (numOfBins);
        }
        List<BucketRange<Double>> buckets = new ArrayList<>();
        for (int i = 0; i < bucketsData.length - 1; i++) {
            double start = bucketsData[i];
            double end = bucketsData[i + 1];
            BucketRange bucket = new BucketRange<>(start, end);
            buckets.add(bucket);
        }
        return buckets;
    }
    //endregion

    //region Private Methods
    private static String calcBucketStart(int numChars, int startCode, int prefixLen, int bucketIdx) {
        char[] chars = new char[prefixLen];
        for (int i = 0; i < prefixLen; i++) {
            int code = startCode + (Math.floorDiv(bucketIdx, (int) Math.pow(numChars, prefixLen - (i + 1)))) % numChars;
            chars[i] = Character.toChars(code)[0];
        }
        return String.valueOf(chars);
    }

    private static String calcBucketEnd(int numChars, int startCode, int prefixLen, int bucketIdx, int interval) {
        char[] chars = new char[prefixLen];
        for (int i = 0; i < prefixLen; i++) {
            int code = startCode + (Math.floorDiv(bucketIdx + interval - 1, (int) Math.pow(numChars, prefixLen - (i + 1)))) % numChars;
            chars[i] = Character.toChars(code)[0];
        }
        return String.valueOf(chars);
    }
    //endregion

}
