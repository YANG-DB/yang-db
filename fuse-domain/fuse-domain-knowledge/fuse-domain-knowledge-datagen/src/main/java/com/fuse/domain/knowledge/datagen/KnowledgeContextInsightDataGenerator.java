package com.fuse.domain.knowledge.datagen;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.fuse.domain.knowledge.datagen.model.Einsight;
import com.fuse.domain.knowledge.datagen.model.Entity;
import com.fuse.domain.knowledge.datagen.model.Insight;
import com.fuse.domain.knowledge.datagen.model.KnowledgeEntityBase;
import com.kayhut.fuse.unipop.controller.search.DefaultSearchOrderProvider;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by Roman on 6/27/2018.
 */
public class KnowledgeContextInsightDataGenerator implements KnowledgeGraphGenerator<Object> {
    //region Constructors
    public KnowledgeContextInsightDataGenerator(
            Client client,
            GenerationContext generationContext,
            Supplier<String> insightIdSupplier,
            Supplier<String> contentSupplier,
            Supplier<Entity> entitySupplier,
            Supplier<Integer> numEntitiesSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier) {
        this.client = client;
        this.generationContext = generationContext;

        this.insightIdSupplier = insightIdSupplier;
        this.contentSupplier = contentSupplier;
        this.entitySupplier = entitySupplier;
        this.numEntitiesSupplier = numEntitiesSupplier;
        this.metadataSupplier = metadataSupplier;

        this.numToGenerate = (int) Math.floor(
                this.generationContext.getContextStatistics().getInsightEntityCounts().size() *
                        this.generationContext.getContextGenerationConfiguration().getScaleFactor());
    }
    //endregion

    //region KnowledgeGraphGenerator Implementation
    @Override
    public Iterable<ElasticDocument<KnowledgeEntityBase>> generate(Object obj) {
        if (this.numGenerated >= this.numToGenerate) {
            return Collections.emptyList();
        }

        List<ElasticDocument<KnowledgeEntityBase>> insights = new ArrayList<>();
        while (this.numGenerated < this.numToGenerate && insights.size() < 1000) {
            String context = this.generationContext.getContextGenerationConfiguration().getToContext();
            KnowledgeEntityBase.Metadata metadata = this.metadataSupplier.get();

            List<Entity> entitiesInInsight = Stream.fill(this.numEntitiesSupplier.get(), this.entitySupplier).toJavaList();
            String insightId = this.insightIdSupplier.get();

            insights.add(new ElasticDocument<>(
                    generationContext.getElasticConfiguration().getWriteSchema().getInsightIndex(),
                    "pge",
                    insightId,
                    null,
                    new Insight(
                            context,
                            this.contentSupplier.get(),
                            Stream.ofAll(entitiesInInsight).map(entity -> String.format("%s.%s", entity.getLogicalId(), context)).toJavaList(),
                            metadata)));

            insights.addAll(Stream.ofAll(entitiesInInsight)
                    .map(entity -> new ElasticDocument<KnowledgeEntityBase>(
                            generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex(),
                            "pge",
                            String.format("%s.%s", entity.getLogicalId(), insightId),
                            entity.getLogicalId(),
                            new Einsight(String.format("%s.%s", entity.getLogicalId(), context), insightId)))
                    .toJavaList());

            this.numGenerated += entitiesInInsight.size() + 1;
        }

        return insights;
    }
    //endregion

    //region Fields
    private Client client;
    private GenerationContext generationContext;

    private Supplier<String> insightIdSupplier;
    private Supplier<String> contentSupplier;
    private Supplier<Entity> entitySupplier;
    private Supplier<Integer> numEntitiesSupplier;
    private Supplier<KnowledgeEntityBase.Metadata> metadataSupplier;

    private int numGenerated;
    private int numToGenerate;
    //endregion
}
