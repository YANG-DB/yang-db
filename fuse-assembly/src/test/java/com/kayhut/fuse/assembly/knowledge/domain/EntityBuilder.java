package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import javaslang.collection.Stream;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kayhut.fuse.assembly.knowlegde.KnowledgeDataInfraManager.PGE;

//todo - for kobi usage
public class EntityBuilder extends Metadata {
    public static final String INDEX = "e0";

    public String logicalId;
    public String category = "person";
    public String type = "entity";
    public String context = "global";
    public List<String> refs = new ArrayList<>();

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

    public String id() {
        return logicalId + "." + context;
    }

    public String toString(ObjectMapper mapper) throws JsonProcessingException {
        ArrayNode authNode = mapper.createArrayNode();
        for (String auth : authorization) {
            authNode.add(auth);
        }

        ArrayNode refsNode = mapper.createArrayNode();
        for (String ref : refs) {
            refsNode.add(ref);
        }

        //create knowledge entity
        ObjectNode on = mapper.createObjectNode();
        on.put("type", type);
        on.put("logicalId", logicalId);
        on.put("context", context);
        on.put("category", category);
        on.put("lastUpdateUser", lastUpdateUser);
        on.put("creationUser", creationUser);
        on.put("lastUpdateTime", lastUpdateTime);
        on.put("creationTime", creationTime);
        on.put("authorizationCount", authNode.size());
        on.put("authorization", authNode); // Authorization = Clearance
        on.put("refs", refsNode);

        return mapper.writeValueAsString(on);
    }

    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of("A").toJavaSet())
                .withEType("Entity")
                .withProperties(Arrays.asList(
                        new Property("lastUpdateUser", "raw", lastUpdateUser),
                        new Property("category", "raw", category),
                        new Property("logicalId", "raw", logicalId),
                        new Property("context", "raw", context),
                        new Property("creationUser", "raw", creationUser),
                        new Property("lastUpdateTime", "raw", lastUpdateTime),
                        new Property("creationTime", "raw", creationTime),
                        new Property("refs", "raw", !refs.isEmpty() ? Arrays.asList(refs) : null),
                        new Property("authorization", "raw", Arrays.asList(authorization))
                )).build();
    }
}
