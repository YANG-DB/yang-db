package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;

import java.util.Optional;

//todo - for kobi usage
public class ValueBuilder extends EntityId {
    public static String physicalType = "e.value";
    public static String type = "Evalue";

    public String stringValue;
    public String category;
    public String context;
    public String[] refs;
    private String valueId;

    public static ValueBuilder _v(String stringValue){
        final ValueBuilder builder = new ValueBuilder();
        builder.stringValue = stringValue;
        return builder;
    }

    public ValueBuilder id(String logicalId) {
        this.logicalId = logicalId;
        return this;
    }

    public ValueBuilder cat(String category) {
        this.category = category;
        return this;
    }

    public ValueBuilder ctx(String context) {
        this.context = context;
        return this;
    }

    public ValueBuilder ref(String ... ref) {
        this.refs = ref;
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String id() {
        return valueId;
    }

    @Override
    public Optional<String> routing() {
        return Optional.of(logicalId);
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        return super.collect(mapper, node);
    }

    @Override
    public Entity toEntity() {
        return null;
    }

    @Override
    public String getETag() {
        return "Value."+id();
    }
}
