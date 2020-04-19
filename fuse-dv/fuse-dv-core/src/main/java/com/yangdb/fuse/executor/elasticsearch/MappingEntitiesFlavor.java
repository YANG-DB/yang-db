package com.yangdb.fuse.executor.elasticsearch;

import java.util.Arrays;

/**
 * mapping type flavor for Entities indexing strategy in a document like DB
 */
public enum MappingEntitiesFlavor {
    INDEX,
    NESTED,
    EMBEDDED,
    CHILD;

    public static MappingEntitiesFlavor of(String value) {
        return MappingEntitiesFlavor.valueOf(value.toUpperCase());
    }

    public String getType() {
        return this.name().toLowerCase();
    }
}
