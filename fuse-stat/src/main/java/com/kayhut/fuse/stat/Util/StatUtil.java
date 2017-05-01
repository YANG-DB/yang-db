package com.kayhut.fuse.stat.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.model.Field;
import com.kayhut.fuse.stat.model.HistogramType;
import com.kayhut.fuse.stat.model.StatContainer;
import com.kayhut.fuse.stat.model.Type;
import javaslang.collection.Stream;
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
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static java.util.Optional.ofNullable;

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

    public static TransportClient getClient(Configuration configuration) throws UnknownHostException {
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

    public static void bulkIndexing(TransportClient client, String filePath, String index, String type) throws IOException {
        BulkProcessor bulkProcessor = BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId,
                                           BulkRequest request) {  }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          BulkResponse response) {  }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          Throwable failure) {  }
                })
                .setBulkActions(100)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();

        File file = FileUtils.getFile(filePath);

        LineIterator it = FileUtils.lineIterator(file, "UTF-8");
        try {
            int i = 1;
            while (it.hasNext()) {
                String line = it.nextLine();
                bulkProcessor.add((IndexRequest) new IndexRequest(index, type, String.valueOf(i))
                        .source(line));
                i++;
            }
        } finally {
            it.close();
        }

        bulkProcessor.close();


    }

    public static void showTypeFieldsNames(TransportClient esClient, String indexName, String typeName) {

        List<String> fieldList = new ArrayList<String>();
        ClusterState cs = esClient.admin().cluster().prepareState().setIndices(indexName).execute().actionGet().getState();
        IndexMetaData imd = cs.getMetaData().index(indexName);
        MappingMetaData mdd = imd.mapping(typeName);
        Map<String, Object> map = null;
        try {
            map = mdd.getSourceAsMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fieldList = getList("", map);
        System.out.println("Field List:");
        for (String field : fieldList) {
            System.out.println(field);
        }
    }

    private static List<String> getList(String fieldName, Map<String, Object> mapProperties) {
        List<String> fieldList = new ArrayList<String>();
        Map<String, Object> map = (Map<String, Object>) mapProperties.get("properties");
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (((Map<String, Object>) map.get(key)).containsKey("type")) {
                fieldList.add(fieldName + "" + key);
            } else {
                List<String> tempList = getList(fieldName + "" + key + ".", (Map<String, Object>) map.get(key));
                fieldList.addAll(tempList);
            }
        }
        return fieldList;
    }
}
