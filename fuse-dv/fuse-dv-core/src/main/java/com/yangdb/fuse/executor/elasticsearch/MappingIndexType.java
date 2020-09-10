package com.yangdb.fuse.executor.elasticsearch;

/**
 * the basic index mapping type currently supported for providing the physical elastic ontology schema
 */
public enum MappingIndexType {
    //static index
    STATIC,
    //common general index - unifies all entities under the same physical index
    UNIFIED,
    //time partitioned index
    TIME,
    //internal document which will be flattened to a dot separated key pathe
    NESTED
}
