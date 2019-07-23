package com.yangdb.fuse.assembly.knowledge.domain;

import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.Collections;

public class LogicalEntity extends EntityId {
    public static final String type = "LogicalEntity";
    private String logicalId;

    public LogicalEntity(String logicalId) {
        this.logicalId = logicalId;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String id() {
        return logicalId;
    }

    @Override
    public Entity toEntity() {
        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withProperties(Collections.singletonList(new Property("logicalId", "raw", logicalId)))
                .withEType("LogicalEntity").build();
    }

    @Override
    public String getETag() {
        return "LogicalEntity";
    }
}
