package com.kayhut.fuse.assembly.knowledge.load;

import com.fasterxml.jackson.databind.JsonNode;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;

public class KnowledgeTransformer {

    public KnowledgeContext transform(RawSchema schema,JsonNode graph) {
        //todo populate context according to given json graph
        return new KnowledgeContext();
    }
}
