package com.kayhut.fuse.stat.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.model.configuration.Field;
import com.kayhut.fuse.stat.model.configuration.HistogramType;
import com.kayhut.fuse.stat.model.configuration.StatContainer;
import com.kayhut.fuse.stat.model.configuration.Type;
import com.kayhut.fuse.stat.model.result.BucketStatResult;
import javaslang.collection.Stream;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Base64;

/**
 * Created by benishue on 30-Apr-17.
 */
public class StatUtil {

    public static String readJsonToString(String jsonRelativePath) throws Exception {
        String result = "";
        ClassLoader classLoader = StatUtil.class.getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(jsonRelativePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Optional<StatContainer> getStatConfigurationObject(String statJson) {
        Optional<StatContainer> resultObj = Optional.empty();
        try {
            resultObj = Optional.of(new ObjectMapper().readValue(statJson, StatContainer.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
            return resultObj;
    }

    public static TransportClient getDataClient(Configuration configuration) throws UnknownHostException {
        String clusterName = configuration.getString("es.cluster.name");
        int transportPort = configuration.getInt("es.client.transport.port");
        String[] hosts = configuration.getStringArray("es.nodes.hosts");

        Settings settings = Settings.builder().put("client.transport.sniff", true).put("cluster.name", clusterName).build();
        TransportClient esClient = TransportClient.builder().settings(settings).build();
        for(String node: hosts){
            esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node), transportPort));
        }

        return esClient;
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

    public static Optional<List<Field>> getFieldsWithManualHistogramOfType(StatContainer statContainer, String typeName){
        return  getFieldsWithHistogramOfType(statContainer,typeName,HistogramType.manual);
    }

    public static Iterable<Map<String, Object>> createBuckets(List<BucketStatResult> bucketStatResults) {
        List<Map<String, Object>> buckets = new ArrayList<>();
        for (BucketStatResult bucketStatResult : bucketStatResults) {
            Map<String, Object> bucket = new HashedMap();
            bucket.put("id", createBucketUniqueId(bucketStatResult.getIndex(),bucketStatResult.getType(),bucketStatResult.getField(),bucketStatResult.getKey(), bucketStatResult.getLowerBound(),bucketStatResult.getUpperBound()));
            bucket.put("index", bucketStatResult.getIndex());
            bucket.put("type", bucketStatResult.getType());
            bucket.put("field", bucketStatResult.getField());
            bucket.put("bucket", bucketStatResult.getKey());
            bucket.put("upper_bound", bucketStatResult.getUpperBound());
            bucket.put("lower_bound", bucketStatResult.getLowerBound());
            bucket.put("count", bucketStatResult.getCount());
            bucket.put("cardinality", bucketStatResult.getCardinality());
            buckets.add(bucket);
        }
        return buckets;
    }

    public static String createBucketUniqueId(String indexName, String typeName, String fieldName, String bucketKey, String lowerBound, String upperBound){
        return hashMessage(indexName +typeName + fieldName + bucketKey + lowerBound + upperBound);
    }

   //Create a MD5 hash of a given message. Used for creating unique document IDs.
    public static String hashMessage(String message) {
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

}
