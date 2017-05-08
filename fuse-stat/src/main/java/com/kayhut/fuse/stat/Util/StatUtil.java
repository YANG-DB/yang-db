package com.kayhut.fuse.stat.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.model.configuration.*;
import com.kayhut.fuse.stat.model.result.StringStatResult;
import javaslang.collection.Stream;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by benishue on 30-Apr-17.
 */
public class StatUtil {

    public static String readJsonToString(String jsonRelativePath) throws Exception {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(jsonRelativePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    public static Optional<StatContainer> getStatConfigurationObject(Configuration configuration) {
        Optional<StatContainer> resultObj = Optional.empty();
        try {
            String statConfigurationFilePath = configuration.getString("statistics.configuration.file");
            String statJson = readJsonToString(statConfigurationFilePath);
            resultObj = Optional.of(new ObjectMapper().readValue(statJson, StatContainer.class));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultObj;
    }

    public static Optional<Type> getTypeConfiguration(StatContainer statContainer, String typeName){
        Optional<Type> typeElement = Optional.empty();
        List<Type> types = Stream.ofAll(statContainer.getTypes()).filter(type -> type.getType().equals(typeName)).toJavaList();
        if (types.size()>0)
            typeElement = Optional.ofNullable(types.get(0));
        return typeElement;
    }

    public static Optional<Field> geFieldConfiguration(StatContainer statContainer, String typeName ,String fieldName){
        Optional<Field> fieldElement = Optional.empty();
        Optional<Type> typeElement = getTypeConfiguration(statContainer,typeName);
        if (typeElement.isPresent()){
            List<Field> fields = Stream.ofAll(typeElement.get().getFields()).filter(field -> field.getField().equals(fieldName)).toJavaList();
            if (fields.size()>0) {
                fieldElement = Optional.ofNullable(fields.get(0));
            }
        }
        return fieldElement;
    }

    public static Optional<List<Field>> getFieldsWithHistogramOfType(StatContainer statContainer, String typeName, HistogramType histogramType ){
        Optional<List<Field>> fields = Optional.empty();
        Optional<Type> typeElement = getTypeConfiguration(statContainer,typeName);
        if (typeElement.isPresent()){
            fields = Optional.ofNullable(Stream.ofAll(typeElement.get().getFields()).filter(field -> field.getHistogram().getHistogramType() == histogramType).toJavaList());
        }
        return  fields;
    }

    public static Optional<List<Field>> getFieldsWithNumericHistogramOfType(StatContainer statContainer, String typeName){
        return  getFieldsWithHistogramOfType(statContainer,typeName,HistogramType.numeric);
    }

    public static Optional<List<Field>> getFieldsWithStringHistogramOfType(StatContainer statContainer, String typeName){
        return  getFieldsWithHistogramOfType(statContainer,typeName,HistogramType.string);
    }

    public static Optional<List<Field>> getFieldsWithCompositeHistogramOfType(StatContainer statContainer, String typeName){
        return  getFieldsWithHistogramOfType(statContainer,typeName,HistogramType.composite);
    }

    public static Optional<List<Field>> getFieldsWithManualHistogramOfType(StatContainer statContainer, String typeName){
        return  getFieldsWithHistogramOfType(statContainer,typeName,HistogramType.manual);
    }

    public static Iterable<Map<String, Object>> prepareStatDocs(List<StringStatResult> bucketStatResults) {
        List<Map<String, Object>> buckets = new ArrayList<>();
        for (StringStatResult bucketStatResult : bucketStatResults) {
            Map<String, Object> bucket = new HashedMap();
            String bucketId = createBucketUniqueId(bucketStatResult.getIndex(),bucketStatResult.getType(),bucketStatResult.getField(), bucketStatResult.getLowerBound(),bucketStatResult.getUpperBound());
            bucket.put("id", bucketId);
            bucket.put("index", bucketStatResult.getIndex());
            bucket.put("type", bucketStatResult.getType());
            bucket.put("field", bucketStatResult.getField());
            bucket.put("upper_bound", bucketStatResult.getUpperBound());
            bucket.put("lower_bound", bucketStatResult.getLowerBound());
            bucket.put("count", bucketStatResult.getDocCount());
            bucket.put("cardinality", bucketStatResult.getCardinality());
            buckets.add(bucket);
        }
        return buckets;
    }

    public static String createBucketUniqueId(String indexName, String typeName, String fieldName, String lowerBound, String upperBound){
        return hashString(indexName + typeName + fieldName + lowerBound + upperBound);
    }

    public static Optional<Field> getFieldByName(StatContainer statContainer, String typeName, String fieldName) {
        Optional<Field> field = Optional.empty();
        Optional<Type> typeElement = getTypeConfiguration(statContainer, typeName);
        if (typeElement.isPresent()) {
            field = typeElement.get().getFields().stream().filter(f -> f.getField().equals(fieldName)).findFirst();
        }
        return field;
    }

    public static List<Bucket> calculateAlphabeticBuckets(int startCode, int numChars, int prefixLen, int interval) {
        List<Bucket> buckets = new ArrayList<>();

        int numOfBuckets = (int) Math.ceil(Math.pow(numChars, prefixLen) / interval);
        for (int i = 0; i < numOfBuckets; i++) {
            int bucketIdx = i * interval;
            buckets.add(new Bucket(calcBucketStart(numChars, startCode, prefixLen, bucketIdx),
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

    private static String calcBucketStart(int numChars, int startCode, int prefixLen, int bucketIdx ) {
        char[] chars = new char[prefixLen];
        for (int i = 0; i < prefixLen; i++) {
            int code = startCode + (Math.floorDiv(bucketIdx, (int) Math.pow(numChars, prefixLen - (i + 1)))) % numChars;
            chars[i] = Character.toChars(code)[0];
        }
        return String.valueOf(chars);
    }

    private static String calcBucketEnd(int numChars, int startCode,int prefixLen, int bucketIdx, int interval) {
        char[] chars = new char[prefixLen];
        for (int i = 0; i < prefixLen; i++) {
            int code = startCode + (Math.floorDiv(bucketIdx + interval - 1, (int) Math.pow(numChars, prefixLen - (i + 1)))) % numChars;
            chars[i] = Character.toChars(code)[0];
        }
        return String.valueOf(chars);
    }

   //Create a MD5 hash of a given message. Used for creating unique document IDs.
    public static String hashString(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bucketDescriptionBytes = message.getBytes("UTF8");
            byte[] bucketHash = digest.digest(bucketDescriptionBytes);

            return org.elasticsearch.common.Base64.encodeBytes(bucketHash, org.elasticsearch.common.Base64.URL_SAFE).replaceAll("\\s", "");

        } catch (NoSuchAlgorithmException e) {

//            logger.error("Could not hash the message: {}", message);
//            logger.error("The hash algorithm used is not supported. Stack trace follows.", e);

            return null;
        } catch (UnsupportedEncodingException e) {

//            logger.error("Could not hash the message: {}", message);
//            logger.error("The character encoding used is not supported. Stack trace follows.", e);

            return null;
        } catch (IOException e) {

//            logger.error("Could not hash the message: {}", message);
//            logger.error("A problem occured when encoding as URL safe hash. Stack trace follows.", e);

            return null;
        }
    }

    public static List<Bucket> createNumericBuckets(double min, double max, int numOfBins) {
        List<Bucket> buckets = new ArrayList<>();
        double[] bucketsData = new double[numOfBins];
        for (int i = 0; i < numOfBins; i++){
            bucketsData[i] = min + i * (max - min) / (numOfBins - 1);
        }

        for (int i = 0; i < bucketsData.length -1; i++){
            int start = (int)bucketsData[i];
            int end = (int)bucketsData[i+1];
            Bucket bucket = new Bucket(Integer.toString(start), Integer.toString(end));
            buckets.add(bucket);
        }
        return buckets;
    }
}
