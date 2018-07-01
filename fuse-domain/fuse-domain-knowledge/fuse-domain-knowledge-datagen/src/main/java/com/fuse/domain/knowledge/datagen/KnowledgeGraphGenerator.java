package com.fuse.domain.knowledge.datagen;

import com.fuse.domain.knowledge.datagen.model.KnowledgeEntityBase;

public interface KnowledgeGraphGenerator<TGenerationContext> {
    Iterable<ElasticDocument<KnowledgeEntityBase>> generate(TGenerationContext generationContext);
}
