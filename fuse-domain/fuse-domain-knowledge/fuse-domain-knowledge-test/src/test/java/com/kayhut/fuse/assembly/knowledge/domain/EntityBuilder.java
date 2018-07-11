package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import com.kayhut.fuse.model.results.Relationship;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//todo - for kobi usage
public class EntityBuilder extends EntityId {
    public static final String INDEX = "e0";

    public String logicalId;
    public String category = "person";
    public String type = "entity";
    public String context = "global";
    public List<String> refs = new ArrayList<>();

    public List<Entity> subEntities = new ArrayList<>();
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
                .withRType("has" + ENTITY + "Reference")
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
                .withRType("hasEntityFile")
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
        return "E" + id();
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);
        ArrayNode refsNode = mapper.createArrayNode();
        for (String ref : refs) {
            refsNode.add(ref);
        }

        //create knowledge entity
        on.put("type", type);
        on.put("logicalId", logicalId);
        on.put("context", context);
        on.put("category", category);
        on.put("refs", refsNode);
        return on;
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
        return Stream.ofAll(hasGlobal)
                .appendAll(hasValues)
                .appendAll(hasRefs)
                .appendAll(hasInsights)
                .appendAll(hasFiles)
                .toJavaList();
    }

    public List<Entity> subEntities() {
        return subEntities;
    }

}
