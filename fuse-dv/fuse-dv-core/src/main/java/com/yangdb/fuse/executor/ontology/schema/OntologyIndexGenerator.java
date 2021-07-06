package com.yangdb.fuse.executor.ontology.schema;

import javaslang.Tuple2;

import java.util.List;

/**
 * generate projected mapping / index according ontology
 */
public interface OntologyIndexGenerator {
    List<Tuple2<String, Boolean>> generateMappings();

    List<Tuple2<Boolean, String>> createIndices();


}
