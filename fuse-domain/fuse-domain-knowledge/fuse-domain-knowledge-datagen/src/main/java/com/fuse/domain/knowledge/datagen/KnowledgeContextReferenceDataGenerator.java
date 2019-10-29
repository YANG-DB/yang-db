package com.fuse.domain.knowledge.datagen;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fuse.domain.knowledge.datagen.model.KnowledgeEntityBase;
import com.fuse.domain.knowledge.datagen.model.Reference;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class KnowledgeContextReferenceDataGenerator implements KnowledgeGraphGenerator<Object> {
    //region Constructors
    public KnowledgeContextReferenceDataGenerator(
            Client client,
            GenerationContext generationContext,
            Supplier<String> referenceIdSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier,
            Supplier<String> titleSupplier,
            Supplier<String> contentSupplier,
            Supplier<String> urlSupplier,
            Supplier<String> systemSupplier) {
        this.client = client;
        this.generationContext = generationContext;
        this.referenceIdSupplier = referenceIdSupplier;
        this.metadataSupplier = metadataSupplier;
        this.titleSupplier = titleSupplier;
        this.contentSupplier = contentSupplier;
        this.urlSupplier = urlSupplier;
        this.systemSupplier = systemSupplier;

        this.numToGenerate = (int)Math.floor(
                generationContext.getContextStatistics().getDistinctNumReferences() *
                        generationContext.getContextGenerationConfiguration().getScaleFactor());
    }
    //endregion

    //region KnowledgeGraphGenerator Implementation
    @Override
    public Iterable<ElasticDocument<KnowledgeEntityBase>> generate(Object o) {
        if (this.numGenerated >= this.numToGenerate) {
            return Collections.emptyList();
        }

        List<ElasticDocument<KnowledgeEntityBase>> references = new ArrayList<>();
        while(this.numGenerated < this.numToGenerate && references.size() < 1000) {
            references.add(new ElasticDocument<>(
                    this.generationContext.getElasticConfiguration().getWriteSchema().getReferenceIndex(),
                    "pge",
                    this.referenceIdSupplier.get(),
                    null,
                    new Reference(
                            this.titleSupplier.get(),
                            this.contentSupplier.get(),
                            this.urlSupplier.get(),
                            this.systemSupplier.get(),
                            this.metadataSupplier.get())));

            this.numGenerated++;
        }

        return references;
    }
    //endregion

    //region Fields
    private Client client;
    private GenerationContext generationContext;

    private Supplier<String> referenceIdSupplier;
    private Supplier<KnowledgeEntityBase.Metadata> metadataSupplier;
    private Supplier<String> titleSupplier;
    private Supplier<String> contentSupplier;
    private Supplier<String> urlSupplier;
    private Supplier<String> systemSupplier;

    private int numGenerated;
    private int numToGenerate;
    //endregion
}
