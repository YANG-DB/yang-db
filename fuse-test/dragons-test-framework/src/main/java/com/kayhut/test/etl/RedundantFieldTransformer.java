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

import java.util.ArrayList;
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

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch((String[]) indices.toArray()).
                                                            setTypes(type).
                                                            setSearchType(SearchType.DEFAULT)
                                                            .setQuery(QueryBuilders.idsQuery(type).addIds(idValues));

        //dupFields.keySet().forEach(k -> searchRequestBuilder.addField(k));
        try {
            SearchResponse searchResponse = searchRequestBuilder.setSize(idValues.size()).execute().get();

            System.out.println(searchResponse);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        CsvSchema.Builder builder = CsvSchema.builder();
        oldSchema.forEach(c -> builder.addColumn(c));
        dupFields.values().forEach(v -> builder.addColumn(v));
        return builder.build();
    }
}
