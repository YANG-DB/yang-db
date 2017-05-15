package com.kayhut.test.framework.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Roman on 12/04/2017.
 */
public class MappingJsonStringElasticConfigurer extends MappingElasticConfigurer {
    //region Constructors
    public MappingJsonStringElasticConfigurer(String indexName, String mappingJson) throws IOException {
        super(indexName, new ObjectMapper().readValue(mappingJson, new TypeReference<Map<String, Object>>(){}));
    }

    public MappingJsonStringElasticConfigurer(Iterable<String> indices, String mappingJson) throws IOException {
        super(indices, new ObjectMapper().readValue(mappingJson, new TypeReference<Map<String, Object>>(){}));
    }
    //endregion
}
