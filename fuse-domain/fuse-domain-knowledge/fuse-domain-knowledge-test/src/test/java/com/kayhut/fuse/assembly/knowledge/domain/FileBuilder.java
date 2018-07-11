package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Optional;

//todo - for kobi usage
public class FileBuilder extends EntityId {
    public static final String FILE = "File";
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

    @Override
    public Optional<String> routing() {
        return Optional.of(logicalId);
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);
        on.put("type", "file");
        on.put("name", name);
        on.put("path", path);
        on.put("displayName", displayName);
        on.put("mimeType", mimeType);
        on.put("category", category);
        on.put("description", description);
        on.put("logicalId", logicalId);
        on.put("entityId", entityId);
        return on;
    }

    @Override
    public String getETag() {
        return FILE + "#" + id();
    }

    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(fileId)
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType(FILE)
                .withProperties(collect(Arrays.asList(
                        new Property("name", "raw", name),
                        new Property("path", "raw", path),
                        new Property("displayName", "raw", displayName),
                        new Property("mimeType", "raw", mimeType),
                        new Property("category", "raw", category),
                        new Property("description", "raw", description),
                        new Property("logicalId", "raw", logicalId),
                        new Property("entityId", "raw", entityId)
                ))).build();
    }

}
