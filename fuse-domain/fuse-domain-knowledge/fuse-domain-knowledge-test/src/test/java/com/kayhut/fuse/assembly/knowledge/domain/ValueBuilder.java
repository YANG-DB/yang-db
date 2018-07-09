package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//todo - for kobi usage
public class ValueBuilder extends EntityId {
    public static String physicalType = "e.value";
    public static String type = "Evalue";

    public List<String> refs = new ArrayList<>();
    public String context;
    public String fieldId;
    public String bdt;
    public String stringValue;
    public String dateValue;
    public int intValue = Integer.MIN_VALUE;
    private String valueId;

    public static ValueBuilder _v(String valueId){
        final ValueBuilder builder = new ValueBuilder();
        builder.valueId = valueId;
        return builder;
    }

    public ValueBuilder id(String logicalId) {
        this.logicalId = logicalId;
        return this;
    }

    public ValueBuilder value(String value) {
        this.stringValue = value;
        return this;
    }

    public ValueBuilder value(int value) {
        this.intValue = value;
        return this;
    }

    public ValueBuilder bdt(String bdt) {
        this.bdt = bdt;
        return this;
    }

    public ValueBuilder field(String field) {
        this.fieldId = field;
        return this;
    }

    public ValueBuilder ctx(String context) {
        this.context = context;
        return this;
    }

    public ValueBuilder ref(String ... ref) {
        this.refs = Arrays.asList(ref);
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
        ObjectNode on = super.collect(mapper, node);
        //create value entity
        on.put("type", physicalType);
        on.put("logicalId", logicalId);
        on.put("entityId", entityId);
        on.put("fieldId", fieldId);
        on.put("bdt", bdt);
        on.put("context", context);
        on.put("refs", collectRefs(mapper,refs));
        if(stringValue!=null)
            on.put("stringValue", stringValue);
        else if(intValue!=Integer.MIN_VALUE)
            on.put("intValue", intValue);
        else if(dateValue!=null)
            on.put("dateValue", dateValue);
        return on;
    }

    @Override
    public Entity toEntity() {
        Property value = new Property("stringValue", "raw", stringValue);;
        if(stringValue!=null)
            value = new Property("stringValue", "raw", stringValue);
        else if(intValue!=Integer.MIN_VALUE)
            value = new Property("intValue", "raw", intValue);
        else if(dateValue!=null)
            value = new Property("dateValue", "raw", dateValue);

        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType(getType())
                .withProperties(collect(Arrays.asList(
                        value,
                        new Property("context", "raw", context),
                        new Property("logicalId", "raw", logicalId),
                        new Property("entityId", "raw", entityId),
                        new Property("bdt", "raw", bdt),
                        new Property("fieldId", "raw", fieldId),
                        new Property("refs", "raw", !refs.isEmpty() ? refs : null)
                ))).build();
    }

    @Override
    public String getETag() {
        return "Value."+id();
    }
}
