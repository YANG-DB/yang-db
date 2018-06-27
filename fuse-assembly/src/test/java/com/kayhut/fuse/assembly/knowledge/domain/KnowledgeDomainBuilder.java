package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;

import java.util.Optional;

//todo - for kobi usage
public abstract class KnowledgeDomainBuilder {
    public String type;

    public abstract String id();

    public Optional<String> routing() {
        return Optional.empty();
    }

    public final String toString(ObjectMapper mapper) throws JsonProcessingException {
        ObjectNode on = mapper.createObjectNode();
        return mapper.writeValueAsString(collect(mapper,on));
    }

    public abstract Entity toEntity();

    public abstract String getETag();

    public abstract ObjectNode collect(ObjectMapper mapper, ObjectNode node);


}
