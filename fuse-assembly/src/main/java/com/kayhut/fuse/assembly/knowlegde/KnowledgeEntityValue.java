package com.kayhut.fuse.assembly.knowlegde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by user pc on 5/11/2018.
 */
public class KnowledgeEntityValue {
    private String _id;
    private String _entityId;
    private String _logicalId;
    private EntityValue _value;

    public void setId(String value) {
        _id = value;
    }

    public String getId() {
        return _id;
    }

    public void seteEntityId(String value) {
        _entityId = value;
    }

    public String getEntityId() {
        return _entityId;
    }

    public void setLogicalId(String value) {
        _logicalId = value;
    }

    public String getLogicalId() {
        return _logicalId;
    }

    public EntityValue getEntityValue() {
        if (_value == null) {
            _value = new EntityValue();
        }

        return _value;
    }

    public String getElasticSearchJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(_value);
    }
}
