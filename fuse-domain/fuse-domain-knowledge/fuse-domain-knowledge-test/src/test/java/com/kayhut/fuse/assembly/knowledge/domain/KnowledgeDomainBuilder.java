package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

//todo - for kobi usage
public abstract class KnowledgeDomainBuilder {
    public abstract String getType();

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


    protected ArrayNode collectRefs(ObjectMapper mapper, List<String> refs) {
        ArrayNode refsNode = mapper.createArrayNode();
        for (String ref : refs) {
            refsNode.add(ref);
        }
        return refsNode;
    }

    public List<KnowledgeDomainBuilder> additional() {
        return Collections.emptyList();
    }



}
