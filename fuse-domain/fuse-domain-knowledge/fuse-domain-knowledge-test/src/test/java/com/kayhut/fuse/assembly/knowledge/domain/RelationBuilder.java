package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//todo - for kobi usage
public class RelationBuilder extends Metadata {
    public static final String REL_INDEX = "rel0";

    public static final String type = "Relation";
    public static String physicalType = "relation";

    public String context;
    public String category;
    public String entityAId;
    public String entityBId;
    public String entityACategory;
    public String entityBCategory;
    private String relId;
    public List<String> refs = new ArrayList<>();

    public static RelationBuilder _rel(String id) {
        final RelationBuilder builder = new RelationBuilder();
        builder.relId = id;
        return builder;
    }

    public RelationBuilder sideA(EntityBuilder sideA) {
        this.entityAId = sideA.id();
        this.entityACategory = sideA.category;
        sideA.rel(this);
        return this;
    }

    public RelationBuilder sideB(EntityBuilder sideB) {
        this.entityBId = sideB.id();
        this.entityBCategory = sideB.category;
        sideB.rel(this);
        return this;
    }

    public RelationBuilder cat(String category) {
        this.category = category;
        return this;
    }

    public RelationBuilder context(String context) {
        this.context = context;
        return this;
    }

    public RelationBuilder entityAId(String entityAId) {
        this.entityAId = entityAId;
        return this;
    }

    public RelationBuilder entityBId(String entityBId) {
        this.entityBId = entityBId;
        return this;
    }

   public RelationBuilder entityACategory(String entityACategory) {
        this.entityACategory = entityACategory;
        return this;
    }

    public RelationBuilder entityBCategory(String entityBCategory) {
        this.entityBCategory = entityBCategory;
        return this;
    }

    public RelationBuilder ref(String... ref) {
        this.refs = Arrays.asList(ref);
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String id() {
        return relId;
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);
        //create knowledge entity
        on.put("type", physicalType);
        on.put("context", context);
        on.put("category", category);
        on.put("entityAId", entityAId);
        on.put("entityBId", entityBId);
        on.put("entityACategory", entityACategory);
        on.put("entityBCategory", entityBCategory);
        on.put("refs", collectRefs(mapper,refs));
        //make sure value or content
        return on;
    }

    @Override
    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType(getType())
                .withProperties(collect(Arrays.asList(
                        new Property("context", "raw", context),
                        new Property("category", "raw", category),
                        new Property("entityAId", "raw", entityAId),
                        new Property("entityBId", "raw", entityBId),
                        new Property("entityACategory", "raw", entityACategory),
                        new Property("entityBCategory", "raw", entityBCategory),
                        new Property("refs", "raw", !refs.isEmpty() ? refs : null)
                ))).build();
    }

    @Override
    public String getETag() {
        return "Reference." + id();
    }

}
