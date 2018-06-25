package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.Arrays;

//todo - for kobi usage
public class FileBuilder extends EntityId {
    public String fileId;
    public String type = "file";
    public String name;
    public String path;
    public String displayName;
    public String mimeType;
    public String category;
    public String description;

    public static FileBuilder _f(String fileId) {
        final FileBuilder builder = new FileBuilder();
        builder.fileId = fileId;
        return builder;
    }

    public FileBuilder cat(String category) {
        this.category = category;
        return this;
    }

    public FileBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FileBuilder display(String display) {
        this.displayName = display;
        return this;
    }

    public FileBuilder path(String path) {
        this.path = path;
        return this;
    }

    public FileBuilder mime(String mime) {
        this.mimeType = mime;
        return this;
    }

    public FileBuilder desc(String desc) {
        this.description = desc;
        return this;
    }

    @Override
    public String id() {
        return fileId;
    }

    public String toString(ObjectMapper mapper) throws JsonProcessingException {
        ArrayNode authNode = mapper.createArrayNode();
        for (String auth : authorization) {
            authNode.add(auth);
        }

        //create knowledge entity
        ObjectNode on = mapper.createObjectNode();
        on.put("type", "file");
        on.put("name", name);
        on.put("path", path);
        on.put("displayName", displayName);
        on.put("mimeType", mimeType);
        on.put("category", category);
        on.put("description", description);
        on.put("logicalId", logicalId);
        on.put("entityId", entityId);

            //metadata
        on.put("authorization", authNode);
        on.put("authorizationCount", authNode.size());
        on.put("lastUpdateUser", lastUpdateUser);
        on.put("lastUpdateTime", lastUpdateTime);
        on.put("creationUser", creationUser);
        on.put("creationTime", creationTime);

        return mapper.writeValueAsString(on);
    }

    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(fileId)
                .withETag(Stream.of("A").toJavaSet())
                .withEType("File")
                .withProperties(Arrays.asList(
                        new Property("name", "raw", name),
                        new Property("path", "raw", path),
                        new Property("displayName", "raw", displayName),
                        new Property("mimeType", "raw", mimeType),
                        new Property("category", "raw", category),
                        new Property("description", "raw", description),
                        new Property("logicalId", "raw", logicalId),
                        new Property("entityId", "raw", entityId),
                        new Property("creationUser", "raw", creationUser),
                        new Property("lastUpdateTime", "raw", lastUpdateTime),
                        new Property("creationTime", "raw", creationTime),
                        new Property("lastUpdateUser", "raw", lastUpdateUser),
                        new Property("authorization", "raw", Arrays.asList(authorization))
                )).build();
    }

}
