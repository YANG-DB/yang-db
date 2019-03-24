package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import javaslang.collection.Stream;
import org.geojson.Point;

import java.util.*;


public class RvalueBuilder extends EntityId {
    public static String physicalType = "r.value";
    public static String type = "Rvalue";

    public List<String> refs = new ArrayList<>();
    public String context;
    public String fieldId;
    public String bdt;
    public String stringValue;
    public Point geoValue;
    public Date dateValue;
    public int intValue = Integer.MIN_VALUE;
    public long longValue;
    public float floatValue;
    private String valueId;

    public static RvalueBuilder _r(String valueId){
        final RvalueBuilder builder = new RvalueBuilder();
        builder.valueId = valueId;
        return builder;
    }

    public RvalueBuilder relId(String relationId) {
        this.relationId = relationId;
        return this;
    }


    public RvalueBuilder value(String value) {
        this.stringValue = value;
        return this;
    }

    public RvalueBuilder value(Point value) {
        this.geoValue = value;
        return this;
    }

    public RvalueBuilder value(Date value) {
        this.dateValue = value;
        return this;
    }

    public RvalueBuilder value(int value) {
        this.intValue = value;
        return this;
    }

    public RvalueBuilder value(long value) {
        this.longValue = value;
        return this;
    }
    public RvalueBuilder value(float value) {
        this.floatValue = value;
        return this;
    }

    public RvalueBuilder bdt(String bdt) {
        this.bdt = bdt;
        return this;
    }

    public RvalueBuilder field(String field) {
        this.fieldId = field;
        return this;
    }

    public RvalueBuilder ctx(String context) {
        this.context = context;
        return this;
    }

    public RvalueBuilder ref(String ... ref) {
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
        return Optional.of(relationId);
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);
        //create value entity
        on.put("id", id());
        on.put("type", physicalType);
        on.put("relationId", relationId);
        //on.put("entityId", entityId);
        on.put("fieldId", fieldId);
        on.put("bdt", bdt);
        on.put("context", context);
        on.put("refs", collectRefs(mapper,refs));
        if(stringValue!=null)
            on.put("stringValue", stringValue);
        else if(intValue!=Integer.MIN_VALUE)
            on.put("intValue", intValue);
        else if(longValue!=0)
            on.put("longValue", longValue);
        else if(floatValue!=0.0)
            on.put("floatValue", floatValue);
        else if(dateValue!=null)
            on.put("dateValue", sdf.format(dateValue));
        return on;
    }

    @Override
    public Entity toEntity() {
        Property value = new Property("stringValue", "raw", stringValue);
        if(stringValue!=null)
            value = new Property("stringValue", "raw", stringValue);
        else if(intValue!=Integer.MIN_VALUE)
            value = new Property("intValue", "raw", intValue);
        else if(longValue!=0)
            value = new Property("longValue", "raw", longValue);
        else if(floatValue!=0)
            value = new Property("floatValue", "raw", floatValue);
        else if(dateValue!=null)
            value = new Property("dateValue", "raw", sdf.format(dateValue));

        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType(getType())
                .withProperties(collect(Arrays.asList(
                        value,
                        new Property("context", "raw", context),
                        new Property("relationId", "raw", relationId),
                       //new Property("entityId", "raw", entityId),
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
