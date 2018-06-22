package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;
import com.kayhut.fuse.generator.knowledge.model.Reference;
import org.elasticsearch.client.Client;

import java.util.Collections;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class KnowledgeContextReferenceDataGenerator implements KnowledgeGraphGenerator<Object> {
    //region Constructors
    public KnowledgeContextReferenceDataGenerator(
            Client client,
            GenerationContext generationContext,
            Supplier<String> referenceIdSupplier) {
        this.client = client;
        this.generationContext = generationContext;
        this.referenceIdSupplier = referenceIdSupplier;
    }
    //endregion

    //region KnowledgeGraphGenerator Implementation
    @Override
    public Iterable<ElasticDocument<KnowledgeEntityBase>> generate(Object o) {
        if (this.numGenerated >= this.numToGenerate) {
            return Collections.emptyList();
        }



        return null;
    }
    //endregion

    //region Fields
    private Client client;
    private GenerationContext generationContext;

    private Supplier<String> referenceIdSupplier;

    private int numGenerated;
    private int numToGenerate;
    //endregion
}
