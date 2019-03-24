package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import com.kayhut.fuse.model.results.Relationship;
import javaslang.collection.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


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

    public List<Entity> subEntities = new ArrayList<>();
    public List<Relationship> hasValues = new ArrayList<>();
    public List<Relationship> hasRefs = new ArrayList<>();


    public RelationBuilder(RelationBuilder builder) {
        super(builder);
        this.relId = builder.relId;
        this.category = builder.category;
        this.context = builder.context;
        this.entityAId = builder.entityAId;
        this.entityBId = builder.entityBId;
        this.entityACategory = builder.entityACategory;
        this.entityBCategory = builder.entityBCategory;
        this.refs = Arrays.asList(builder.refs.toArray(new String[builder.refs.size()]));
    }

    public RelationBuilder() {
    }

    public static RelationBuilder _rel(String id) {
        final RelationBuilder builder = new RelationBuilder();
        builder.relId = id;
        return builder;
    }

    public RelationBuilder sideA(EntityBuilder sideA) {
        this.entityAId = sideA.id();
        this.entityACategory = sideA.category;
        return this;
    }

    public RelationBuilder sideB(EntityBuilder sideB) {
        this.entityBId = sideB.id();
        this.entityBCategory = sideB.category;
        return this;
    }

    public RelationBuilder cat(String category) {
        this.category = category;
        return this;
    }

    public RelationBuilder ctx(String context) {
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

    public RelationBuilder reference(RefBuilder ref) {
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
                .withRType("hasRelationReference")
                .build());
        return this;
    }

    public RelationBuilder value(RvalueBuilder... value) {
        Arrays.asList(value).forEach(this::value);
        return this;
    }

    public RelationBuilder value(RvalueBuilder value) {
        value.relationId = this.id();
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
                .withRType("hasRvalue")
                .build());

        return this;
    }

    public List<Relationship> withRelations() {
        return withRelations(o -> true);
    }

    public List<Relationship> withRelations(String relationType, String... outSideId) {
        return withRelations(p -> p.getrType().equals(relationType) && Arrays.asList(outSideId).contains(p.geteID2()));
    }

    public List<Relationship> withRelations(Predicate<Relationship> filter) {
        return Stream.ofAll(hasValues)
                .appendAll(hasRefs)
                .filter(filter)
                .toJavaList();
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
        on.put("id", id());
        on.put("type", physicalType);
        on.put("context", context);
        on.put("category", category);
        on.put("entityAId", entityAId);
        on.put("entityBId", entityBId);
        on.put("entityACategory", entityACategory);
        on.put("entityBCategory", entityBCategory);
        on.put("refs", collectRefs(mapper, refs));
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



    public static class EntityRelationBuilder extends KnowledgeDomainBuilder {
        public static String physicalType = "e.relation";
        private RelationBuilder builder;
        private String dir;

        public EntityRelationBuilder(String entityAId,RelationBuilder source,String dir) {
            this.builder = new RelationBuilder(source);
            this.dir = dir;
            //
            if(!builder.entityAId.equals(entityAId)) {
                String entityACategory = builder.entityACategory;

                builder.entityACategory(builder.entityBCategory);
                builder.entityBCategory(entityACategory);

                builder.entityBId(builder.entityAId);
                builder.entityAId(entityAId);
            }
        }

        @Override
        public String getType() {
            return physicalType;
        }

        @Override
        public String id() {
            return builder.id()+"."+dir;
        }

        @Override
        public Entity toEntity() {
            return null;
        }

        @Override
        public String getETag() {
            return null;
        }

        @Override
        public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
            builder.collect(mapper, node);
            node.put("id", id());
            node.put("type",physicalType);
            node.put("direction",dir);
            node.put("relationId", builder.id());
            return node;
        }

        @Override
        public Optional<String> routing() {
            return Optional.of(this.builder.entityAId.split("\\.")[0]);
        }
    }

}
