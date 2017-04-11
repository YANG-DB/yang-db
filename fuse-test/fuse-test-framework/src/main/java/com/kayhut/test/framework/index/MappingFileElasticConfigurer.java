package com.kayhut.test.framework.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.test.framework.index.ElasticIndexConfigurer;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by moti on 09/04/2017.
 */
public class MappingFileElasticConfigurer implements ElasticIndexConfigurer {
    private String mappingsFile;
    private String indexName;

    public MappingFileElasticConfigurer(String mappingsFile, String indexName) {
        this.mappingsFile = mappingsFile;
        this.indexName = indexName;
    }

    @Override
    public void configure(TransportClient client) {
        try {
            addMappings(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addMappings(TransportClient client) throws java.io.IOException {
        CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(indexName);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> mappings = mapper.readValue(new File(mappingsFile), new TypeReference<Map<String, Object>>(){});
        Map<String, Object> allMappings = (Map<String, Object>)mappings.get("mappings");
        for(Map.Entry<String, Object> mapping : allMappings.entrySet()){
            createIndexRequestBuilder.addMapping(mapping.getKey(), (Map<String, Object>) mapping.getValue());
        }
        createIndexRequestBuilder.execute().actionGet();
    }
}
