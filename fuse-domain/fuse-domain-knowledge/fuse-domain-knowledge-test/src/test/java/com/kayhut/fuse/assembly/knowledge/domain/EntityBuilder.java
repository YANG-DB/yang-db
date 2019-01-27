package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import com.kayhut.fuse.model.results.Relationship;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

//todo - for kobi usage
public class EntityBuilder extends EntityId {
    public static final String INDEX = "e0";
    public static String type = "Entity";
    public static String physicalType = "entity";

    public String logicalId;
    public String category = "person";
    public String context = "global";

    public List<KnowledgeDomainBuilder> additional = new ArrayList<>();
    public List<String> refs = new ArrayList<>();

    public List<Entity> subEntities = new ArrayList<>();
    public List<Relationship> hasRel = new ArrayList<>();
    public List<Relationship> hasGlobal = new ArrayList<>();
    public List<Relationship> hasFiles = new ArrayList<>();
    public List<Relationship> hasValues = new ArrayList<>();
    public List<Relationship> hasRefs = new ArrayList<>();
    public List<Relationship> hasInsights = new ArrayList<>();


    public static EntityBuilder _e(String logicalId) {
        final EntityBuilder builder = new EntityBuilder();
        builder.logicalId = logicalId;
        return builder;
    }

    public EntityBuilder cat(String category) {
        this.category = category;
        return this;
    }

    public EntityBuilder ctx(String context) {
        this.context = context;
        return this;
    }

    public void rel(RelationBuilder relationBuilder, String dir) {
        additional.add(new RelationBuilder.EntityRelationBuilder(this.id(), relationBuilder, dir));
        //add as entities sub resource
        subEntities.add(relationBuilder.toEntity());

        this.hasRel.add(Relationship.Builder.instance()
                .withAgg(false)
                .withDirectional(false)
                .withEID1(this.id())
                .withEID2(relationBuilder.id())
                .withETag1(this.getETag())
                .withETag2(relationBuilder.getETag())
                .withRType("hasRelation")
                .build());
    }

    @Override
    public String getType() {
        return type;
    }

    public EntityBuilder global(EntityBuilder global) {
        //add global entity
        final LogicalEntity logicalEntity = new LogicalEntity(global.logicalId);
        subEntities.add(logicalEntity.toEntity());

        //add relationship
        hasGlobal.add(Relationship.Builder.instance()
                .withAgg(false)
                .withDirectional(false)
                .withEID1(logicalEntity.id())
                .withEID2(this.id())
                .withETag1(logicalEntity.getETag())
                .withETag2(this.getETag())
                .withRType("hasEntity")
                .build());
        hasGlobal.add(Relationship.Builder.instance()
                .withAgg(false)
                .withDirectional(false)
                .withEID1(logicalEntity.id())
                .withEID2(global.id())
                .withETag1(logicalEntity.getETag())
                .withETag2(global.getETag())
                .withRType("hasEntity")
                .build());

        return this;
    }

    public EntityBuilder ref(String... ref) {
        this.refs = Arrays.asList(ref);
        return this;
    }

    public EntityBuilder reference(RefBuilder ref) {
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
                .withRType("hasEntityReference")
                .build());
        return this;
    }

    public EntityBuilder value(ValueBuilder... value) {
        Arrays.asList(value).forEach(this::value);
        return this;
    }

    public ValueBuilder withValue(ValueBuilder value) {
        value(value);
        return value;
    }

    public EntityBuilder value(ValueBuilder value) {
        value.entityId = id();
        value.logicalId = this.logicalId;
        value.context = this.context;

        //add as entities sub resource
        subEntities.add(value.toEntity());
        //add a relation
        hasValues.add(Relationship.Builder.instance()
                .withAgg(false)
                .withDirectional(false)
                .withEID1(id())
                .withEID2(value.id())
                .withETag1(getETag())
                .withETag2(value.getETag())
                .withRType("hasEvalue")
                .build());

        return this;
    }

    public EntityBuilder insight(InsightBuilder insight) {
        additional.add(new InsightBuilder.EntityInsightBuilder(logicalId, context, insight.id()));
        //add as entities sub resource
        subEntities.add(insight.toEntity());
        //add a relation
        hasInsights.add(Relationship.Builder.instance()
                .withAgg(false)
                .withDirectional(false)
                .withEID1(id())
                .withEID2(insight.id())
                .withETag1(getETag())
                .withETag2(insight.getETag())
                .withRType("hasInsight")
                .build());

        return this;
    }

    public EntityBuilder file(FileBuilder file) {
        file.entityId = id();
        file.logicalId = logicalId;
        //add as entities sub resource
        subEntities.add(file.toEntity());
        //add a relation
        hasFiles.add(Relationship.Builder.instance()
                .withAgg(false)
                .withDirectional(false)
                .withEID1(id())
                .withEID2(file.fileId)
                .withETag1(getETag())
                .withETag2(file.getETag())
                .withRType("hasEfile")
                .build());

        return this;
    }

    public String id() {
        return logicalId + "." + context;
    }

    @Override
    public Optional<String> routing() {
        return Optional.of(logicalId);
    }

    public String getETag() {
        return "Entity." + id();
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);
        //create knowledge entity
        on.put("id", id());
        on.put("type", physicalType);
        on.put("logicalId", logicalId);
        on.put("context", context);
        on.put("category", category);
        on.put("refs", collectRefs(mapper, refs));
        return on;
    }

    @Override
    public List<KnowledgeDomainBuilder> additional() {
        return additional;
    }

    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType("Entity")
                .withProperties(
                        collect(Arrays.asList(
                                new Property("category", "raw", category),
                                new Property("logicalId", "raw", logicalId),
                                new Property("context", "raw", context),
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
        return Stream.ofAll(hasGlobal)
                .appendAll(hasValues)
                .appendAll(hasRefs)
                .appendAll(hasRel)
                .appendAll(hasInsights)
                .appendAll(hasFiles)
                .filter(filter)
                .toJavaList();
    }

    public List<Entity> subEntities() {
        return subEntities;
    }

}
