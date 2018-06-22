package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;

public interface KnowledgeGraphGenerator<TGenerationContext> {
    Iterable<ElasticDocument<KnowledgeEntityBase>> generate(TGenerationContext generationContext);
}
