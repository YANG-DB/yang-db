package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.generator.knowledge.model.Entity;
import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;
import com.kayhut.fuse.generator.knowledge.model.Reference;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class KnowledgeContextEntityDataGenerator implements KnowledgeGraphGenerator<Object> {
    //region Constructors
    public KnowledgeContextEntityDataGenerator(
            Client client,
            GenerationContext generationContext,
            Supplier<String> logicalIdSuppliers,
            Supplier<String> categorySupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier) {
        this.client = client;
        this.generationContext = generationContext;

        this.logicalIdSupplier = logicalIdSuppliers;
        this.categorySupplier = categorySupplier;
        this.metadataSupplier = metadataSupplier;

        this.numToGenerate = (int)Math.floor(
                Stream.ofAll(this.generationContext.getContextStatistics().getEntityCategories().values()).sum().intValue() *
                this.generationContext.getContextGenerationConfiguration().getScaleFactor());
    }
    //endregion

    //region KnowledgeGraphGenerator Implementation
    @Override
    public Iterable<ElasticDocument<KnowledgeEntityBase>> generate(Object obj) {
        if (this.numGenerated >= this.numToGenerate) {
            return Collections.emptyList();
        }

        List<ElasticDocument<KnowledgeEntityBase>> entities = new ArrayList<>();
        while(this.numGenerated < this.numToGenerate && entities.size() < 1000) {
            String logicalId = this.logicalIdSupplier.get();
            String context = this.generationContext.getContextGenerationConfiguration().getToContext();

            entities.add(new ElasticDocument<>(
                    this.generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex(),
                    "pge",
                    String.format("%s.%s", logicalId, context),
                    logicalId,
                    new Entity(
                            logicalId,
                            context,
                            this.categorySupplier.get(),
                            this.metadataSupplier.get())));

            this.numGenerated++;
        }

        return entities;
    }
    //endregion

    //region Fields
    private Client client;
    private GenerationContext generationContext;

    private Supplier<String> logicalIdSupplier;
    private Supplier<String> categorySupplier;
    private Supplier<KnowledgeEntityBase.Metadata> metadataSupplier;

    private int numGenerated;
    private int numToGenerate;
    //endregion
}
