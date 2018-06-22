package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;

import java.util.Collections;
import java.util.function.Supplier;

public class KnowledgeContextEntityDataGenerator implements KnowledgeGraphGenerator<Object> {
    //region Constructors
    public KnowledgeContextEntityDataGenerator(
            Client client,
            GenerationContext generationContext,
            Supplier<String> logicalIdSuppliers,
            Supplier<String> entityValueIdSupplier,
            Supplier<String> referenceIdSupplier
            ) {
        this.client = client;
        this.generationContext = generationContext;

        this.logicalIdSupplier = logicalIdSuppliers;
        this.entityValueIdSupplier = entityValueIdSupplier;
        this.referenceIdSupplier = referenceIdSupplier;

        this.numToGenerate = (int)Math.floor(
                Stream.ofAll(this.generationContext.getContextStatistics().getEntityCategories().values()).sum().intValue() *
                this.generationContext.getConfiguration().getScaleFactor());
    }
    //endregion

    //region KnowledgeGraphGenerator Implementation
    @Override
    public Iterable<ElasticDocument<KnowledgeEntityBase>> generate(Object obj) {
        if (this.numGenerated >= this.numToGenerate) {
            return Collections.emptyList();
        }

        return null;
    }
    //endregion

    //region Fields
    private Client client;
    private GenerationContext generationContext;

    private Supplier<String> logicalIdSupplier;
    private Supplier<String> entityValueIdSupplier;
    private Supplier<String> referenceIdSupplier;

    private int numGenerated;
    private int numToGenerate;
    //endregion
}
