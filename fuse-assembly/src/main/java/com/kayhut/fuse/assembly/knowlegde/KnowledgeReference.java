package com.kayhut.fuse.assembly.knowlegde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by user pc on 5/11/2018.
 */
public class KnowledgeReference {
    private Reference _ref;
    private String _id;

    public String getId() {
       return _id;
    }

    public void setId(String value) {
        _id = value;
    }

    public Reference getRef() {
        return _ref;
    }

    public String getReferenceAsElasticJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(_ref);
    }
}
