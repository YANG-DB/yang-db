package com.kayhut.test.etl;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by moti on 6/5/2017.
 */
public class RedundantFieldTransformer implements Transformer{
    private TransportClient client;
    private Map<String, String> dupFields;
    private String dupIdField;
    private String originalIdField;
    private List<String> indices;
    private String type;

    public RedundantFieldTransformer(TransportClient client, Map<String, String> dupFields, String dupIdField, String originalIdField, List<String> indices, String type) {
        this.client = client;
        this.dupFields = dupFields;
        this.dupIdField = dupIdField;
        this.originalIdField = originalIdField;
        this.indices = indices;
        this.type = type;
    }

    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<String> idValues = new ArrayList<>();
        documents.forEach(doc -> idValues.add(doc.get(dupIdField)));
        String[] arr = new String[indices.size()];
        indices.toArray(arr);
        String[]ids = new String[idValues.size()];
        idValues.toArray(ids);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(arr).setTypes(type).
                                                                            setQuery(QueryBuilders.idsQuery(ids)).
                                                                            setSize(ids.length);
        dupFields.keySet().forEach(k -> searchRequestBuilder.addField(k));
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        Map<String, Map<String, String>> dupsMap = new HashMap<>();

        for (SearchHit hit : searchResponse.getHits()) {
            Map<String,String> fields = new HashMap<>();
            dupFields.keySet().forEach(f -> fields.put(f, hit.getFields().get(f).getValue().toString()));
            dupsMap.put(hit.getId(), fields);
        }
        List<Map<String, String>> newDocuments = new ArrayList<>(documents.size());
        for (Map<String, String> document : documents) {
            Map<String, String> newDocument = new HashMap<>(document);
            Map<String, String> fields = dupsMap.get(document.get(dupIdField));
            dupFields.forEach((k,v) -> {
                newDocument.put(v, fields.get(k));
            });
            newDocuments.add(newDocument);
        }
        return newDocuments;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        CsvSchema.Builder builder = CsvSchema.builder();
        oldSchema.forEach(c -> builder.addColumn(c));
        dupFields.values().forEach(v -> builder.addColumn(v));
        return builder.build();
    }
}
