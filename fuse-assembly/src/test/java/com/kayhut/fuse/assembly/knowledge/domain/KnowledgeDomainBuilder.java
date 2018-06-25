package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//todo - for kobi usage
public abstract class KnowledgeDomainBuilder {
    public String type;

    public abstract String id();

    public abstract String toString(ObjectMapper mapper) throws JsonProcessingException;

}
