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
public class MappingFileElasticConfigurer extends MappingElasticConfigurer {
    //region Constructors
    public MappingFileElasticConfigurer(String indexName, String mappingsFile) throws IOException {
        super(indexName, (Map<String, Object>) new ObjectMapper().readValue(new File(mappingsFile), new TypeReference<Map<String, Object>>(){}));
    }

    public MappingFileElasticConfigurer(Iterable<String> indices, String mappingsFile) throws IOException {
        super(indices, (Map<String, Object>) new ObjectMapper().readValue(new File(mappingsFile), new TypeReference<Map<String, Object>>(){}));
    }
    //endregion
}
