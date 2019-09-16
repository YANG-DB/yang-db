package com.yangdb.fuse.stat.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.stat.model.configuration.Field;
import com.yangdb.fuse.stat.model.configuration.StatContainer;
import com.yangdb.fuse.stat.model.configuration.Type;
import com.yangdb.fuse.stat.model.histogram.Histogram;
import javaslang.collection.Stream;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;

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
            Map<String, Object> dragon = new HashMap<>();
            dragon.put("id", Integer.toString(i));
            dragon.put("type", "Dragon");
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
                fireEdge.put("type", "fire");
                fireEdge.put("timestamp", randomLong(startDate, endDate));
                fireEdge.put("direction", "OUT");
                fireEdge.put("temperature", randomInt(minTemp, maxTemp));

                Map<String, Object> fireEdgeDual = new HashMap<>();
                fireEdgeDual.put("id", "fire_" + +counter + 1);
                fireEdgeDual.put("type", "fire");
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


    public static List<Map<String, Object>> getAllDocs(TransportClient client, String index, String type) {
        int scrollSize = 1000;
        List<Map<String, Object>> esData = new ArrayList<>();
        SearchResponse response = null;
        int i = 0;
        while (response == null || response.getHits().getHits().length != 0) {
            response = client.prepareSearch(index)
                    .setTypes(type)
                    .setQuery(QueryBuilders.matchAllQuery())
                    .setSize(scrollSize)
                    .setFrom(i * scrollSize)
                    .execute()
                    .actionGet();
            for (SearchHit hit : response.getHits()) {
                esData.add(hit.getSourceAsMap());
            }
            i++;
        }
        return esData;
    }


    public static void printAllDocs(TransportClient client, String index, String type) {
        List<Map<String, Object>> allDocs = getAllDocs(client, index, type);
        for ( Map<String, Object> doc : allDocs) {
            for (Map.Entry<String, Object> entry : doc.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
//                System.out.println(key + ": " + value);
                Assert.assertNotNull(key);
                Assert.assertNotNull(value);

                // do stuff
            }
        }
    }

    public static Optional<Histogram> getHistogram(String statJsonConfRelativePath, String type, String field) throws Exception{
        Optional<Histogram> histogram = Optional.empty();

        String statFieldsJson = StatUtil.
                readJsonToString(statJsonConfRelativePath);

        StatContainer statContainer = new ObjectMapper().
                readValue(statFieldsJson, StatContainer.class);
        List<Type> types = Stream.ofAll(statContainer.getTypes()).filter(type1 -> type1.getType().equals(type)).toJavaList();
        List<Field> fields1 = Stream.ofAll(types).flatMap(type1 -> type1.getFields()).filter(field1 -> field1.getField().equals(field)).toJavaList();

        if (!fields1.isEmpty()) {
            histogram = Optional.of(fields1.get(0).getHistogram());
        }

        return histogram;
    }

    public static Set<Map<String, Object>> searchByTerm(TransportClient client, String[] indices, String[] types, String field, String term) {
        SearchResponse response;
        try {
            response = client.prepareSearch()
                    .setIndices(indices)
                    .setQuery(QueryBuilders.boolQuery().filter(QueryBuilders.boolQuery()
                            .must(QueryBuilders.termQuery(field, term))
                            .must(QueryBuilders.termsQuery("type", types))))
                    .execute().actionGet();
        } catch (Throwable e) {
            return new HashSet<>();
        }
        Set<Map<String, Object>> results = new HashSet<>();
        for (SearchHit hit : response.getHits()) {
            Map<String, Object> doc = hit.getSourceAsMap();
            results.add(doc);
        }
        return results;
    }

}
