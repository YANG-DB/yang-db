package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.List;

//todo - for kobi usage
public class InsightBuilder extends EntityId {
    public static final String INSIGNT = "Insignt";
    public String type = "insight";
    public String content;
    public String context;
    public List<String> entityIds;
    private String insightId;

    public static InsightBuilder _i(String id) {
        final InsightBuilder builder = new InsightBuilder();
        builder.insightId = id;
        return builder;
    }

    @Override
    public String id() {
        return insightId;
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);

        ArrayNode entities = mapper.createArrayNode();
        for (String ent : entityIds) {
            entities.add(ent);
        }
        //create knowledge entity
        on.put("type", type);
        on.put("content", content);
        on.put("context", context);
        on.put("entityIds", entities);

        return on;
    }


    @Override
    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType("Insight")
                .withProperties(collect(Arrays.asList(
                        new Property("content", "raw", content),
                        new Property("context", "raw", context),
                        new Property("entityIds", "raw", !entityIds.isEmpty() ? Arrays.asList(entityIds) : null))
                )).build();
    }

    @Override
    public String getETag() {
        return INSIGNT + "#" +id();
    }
}
