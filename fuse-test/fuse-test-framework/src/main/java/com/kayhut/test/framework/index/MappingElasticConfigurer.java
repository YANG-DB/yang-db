package com.kayhut.test.framework.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Roman on 12/04/2017.
 */
public class MappingElasticConfigurer implements ElasticIndexConfigurer {
    //region Constructors
    public MappingElasticConfigurer(String indexName, Map<String, Object> mappings) {
        this.indexName = indexName;
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
        CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(this.indexName);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> allMappings = (Map<String, Object>)mappings.get("mappings");
        for(Map.Entry<String, Object> mapping : allMappings.entrySet()){
            createIndexRequestBuilder.addMapping(mapping.getKey(), (Map<String, Object>) mapping.getValue());
        }
        createIndexRequestBuilder.execute().actionGet();
    }
    //endregion

    //region Fields
    private String indexName;
    private Map<String, Object> mappings;
    //endregion
}
