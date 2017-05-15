package com.kayhut.test.framework.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.collection.Stream;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Roman on 12/04/2017.
 */
public class MappingElasticConfigurer implements ElasticIndexConfigurer {
    //region Constructors
    public MappingElasticConfigurer(String indexName, Map<String, Object> mappings) {
        this(Collections.singletonList(indexName), mappings);
    }

    public MappingElasticConfigurer(Iterable<String> indices, Map<String, Object> mappings) {
        this.indices = Stream.ofAll(indices).toJavaSet();
        this.mappings = mappings;
    }
    //endregion

    //region ElasticIndexConfigurer
    @Override
    public void configure(TransportClient client) {
        try {
            addMappings(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region Private Methods
    protected void addMappings(TransportClient client) throws java.io.IOException {
        for(String indexName : this.indices) {
            CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(indexName);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> allMappings = (Map<String, Object>) mappings.get("mappings");
            for (Map.Entry<String, Object> mapping : allMappings.entrySet()) {
                createIndexRequestBuilder.addMapping(mapping.getKey(), (Map<String, Object>) mapping.getValue());
            }
            createIndexRequestBuilder.execute().actionGet();
        }
    }
    //endregion

    //region Fields
    private Iterable<String> indices;
    private Map<String, Object> mappings;
    //endregion
}
