package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by user pc on 5/11/2018.
 */
public class KnowledgeEntity {
    private String _id;
    private String _logicalId;
    private Entity _entity;

    public String getId() {
        return _id;
    }

    public void setId(String value) {
        _id = value;
    }

    public String getLogicalId() {
        return _logicalId;
    }

    public void setLogicalId(String value) {
        _logicalId = value;
    }

    public Entity getEntity() {
        return _entity;
    }

    public String getElasticSearchJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(_entity);
    }
}
