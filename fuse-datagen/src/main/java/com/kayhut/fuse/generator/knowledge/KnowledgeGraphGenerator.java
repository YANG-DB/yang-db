package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;

public interface KnowledgeGraphGenerator<TGenerationContext> {
    Iterable<KnowledgeEntityBase> generate(TGenerationContext generationContext);
}
