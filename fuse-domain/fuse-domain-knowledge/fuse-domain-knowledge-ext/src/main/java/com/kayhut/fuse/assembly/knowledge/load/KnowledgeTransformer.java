package com.kayhut.fuse.assembly.knowledge.load;

import com.kayhut.fuse.model.logical.LogicalGraphModel;
import com.kayhut.fuse.model.ontology.transformer.OntologyTransformer;

public class KnowledgeTransformer {
    private OntologyTransformer transformer;

    public KnowledgeTransformer(OntologyTransformer transformer) {
        this.transformer = transformer;
    }

    public KnowledgeContext transform(LogicalGraphModel graph) {
        //todo populate context according to given json graph
        graph.getNodes();
        graph.getEdges();
        return new KnowledgeContext();
    }
}
