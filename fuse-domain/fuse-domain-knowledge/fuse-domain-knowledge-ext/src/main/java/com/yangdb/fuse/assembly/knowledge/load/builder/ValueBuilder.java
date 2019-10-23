package com.yangdb.fuse.assembly.knowledge.load.builder;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Property;
import com.yangdb.fuse.model.results.Relationship;
import javaslang.collection.Stream;
import org.geojson.Point;

import java.util.*;
import java.util.function.Predicate;


public class ValueBuilder extends EntityId {
    public final static String physicalType = "e.value";
    public final static String type = "Evalue";

    public List<Relationship> hasRefs = new ArrayList<>();
    public List<Entity> subEntities = new ArrayList<>();

    public List<String> refs = new ArrayList<>();
    public String context = DEFAULT_CTX;
    public String fieldId;
    public String bdt;
    public String stringValue;
    public Point geoValue;
    public Date dateValue;
    public long longValue;
    public float floatValue;
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

    public ValueBuilder value(Point value) {
        this.geoValue = value;
        return this;
    }

    public ValueBuilder value(long value) {
        this.longValue = value;
        return this;
    }

    public ValueBuilder value(float value) {
        this.floatValue = value;
        return this;
    }

    public ValueBuilder value(Date value) {
        this.dateValue = value;
        return this;
    }

    public ValueBuilder value(int value) {
        this.intValue = value;
        return this;
    }

    public ValueBuilder value(Object value) {
        switch (value.getClass().getSimpleName()) {
            case "String":
                return value(value.toString());
            case "Point":
                return value((Point) value);
            case "Date":
                return value((Date) value);
            case "Integer":
                return value((int)value);
            case "Long":
                return value((long)value);
            case "Float":
                return value((float)value);
            default:
                return value(value.toString());
        }
    }

    public ValueBuilder reference(RefBuilder ref) {
        refs.add(ref.id());
        //add as entities sub resource
        subEntities.add(ref.toEntity());
        //add a relation
        hasRefs.add(Relationship.Builder.instance()
                .withAgg(false)
                .withDirectional(false)
                .withEID1(id())
                .withEID2(ref.id())
                .withETag1(getETag())
                .withETag2(ref.getETag())
                .withRType("hasEvalueReference")
                .build());
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
        on.put("id", id());
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
            on.put("dateValue", sdf.format(dateValue));
        else if(geoValue!=null) {
            ObjectNode geo = mapper.createObjectNode();
            geo.put("lat",geoValue.getCoordinates().getLatitude());
            geo.put("lon",geoValue.getCoordinates().getLongitude());
            on.put("geoValue", geo);
        }
/*
        //geo_shape
        else if(geoValue!=null) {
            ObjectNode geo = mapper.createObjectNode();
            geo.put("type","point");
            geo.putArray("coordinates").add(geoValue.getCoordinates().getLatitude()).add(geoValue.getCoordinates().getLongitude());
            on.put("geoValue", geo);
        }
*/
        return on;
    }

    @Override
    public Entity toEntity() {
        Property value = new Property("stringValue", "raw", stringValue);
        if(stringValue!=null)
            value = new Property("stringValue", "raw", stringValue);
        else if(intValue!=Integer.MIN_VALUE)
            value = new Property("intValue", "raw", intValue);
        else if(dateValue!=null)
            value = new Property("dateValue", "raw", sdf.format(dateValue));

        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType(getType())
                .withProperties(collect(Arrays.asList(
                        value,
                        new Property("context", "raw", context),
                        new Property("logicalId", "raw", logicalId),
                        new Property("bdt", "raw", bdt),
                        new Property("fieldId", "raw", fieldId),
                        new Property("refs", "raw", !refs.isEmpty() ? refs : null)
                ))).build();
    }


    public List<Relationship> withRelations() {
        return withRelations(o -> true);
    }

    public List<Relationship> withRelations(String relationType, String... outSideId) {
        return withRelations(p -> p.getrType().equals(relationType) && Arrays.asList(outSideId).contains(p.geteID2()));
    }

    public List<Relationship> withRelations(Predicate<Relationship> filter) {
        return Stream.ofAll(hasRefs)
                .filter(filter)
                .toJavaList();
    }

    @Override
    public String getETag() {
        return "Value."+id();
    }
}
