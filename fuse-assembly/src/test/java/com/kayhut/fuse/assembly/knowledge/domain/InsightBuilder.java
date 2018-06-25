package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//todo - for kobi usage
public class InsightBuilder extends EntityId {
    public String type = "insight";
    public String name;
    public String path;
    public String displayName;
    public String mimeType;
    public String category;
    public String description;
    private String insightId;

    @Override
    public String id() {
        return insightId;
    }

    @Override
    public String toString(ObjectMapper mapper) throws JsonProcessingException {
        return null;
    }
}
