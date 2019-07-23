package com.fuse.domain.knowledge.datagen;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.fuse.domain.knowledge.datagen.model.Entity;
import com.fuse.domain.knowledge.datagen.model.Erelation;
import com.fuse.domain.knowledge.datagen.model.KnowledgeEntityBase;
import com.fuse.domain.knowledge.datagen.model.Relation;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/24/2018.
 */
public class KnowledgeContextRelationDataGenerator implements KnowledgeGraphGenerator<Object> {
    //region Constructors
    public KnowledgeContextRelationDataGenerator(
            Client client,
            GenerationContext generationContext,
            Supplier<String> relationIdSupplier,
            Supplier<String> categorySupplier,
            Supplier<Entity> entitySupplier,
            Supplier<Integer> numOutRelationsSupplier,
            Supplier<Integer> numInRelationsSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier) {
        this.client = client;
        this.generationContext = generationContext;

        this.relationIdSupplier = relationIdSupplier;
        this.categorySupplier = categorySupplier;
        this.entitySupplier = entitySupplier;
        this.metadataSupplier = metadataSupplier;

        this.numOutRelationsSupplier = numOutRelationsSupplier;
        this.numInRelationsSupplier = numInRelationsSupplier;

        this.numToGenerate = (int) Math.floor(
                Stream.ofAll(this.generationContext.getContextStatistics().getRelationCategories().values()).sum().intValue() *
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
        while (this.numGenerated < this.numToGenerate && entities.size() < 1000) {
            String context = this.generationContext.getContextGenerationConfiguration().getToContext();
            KnowledgeEntityBase.Metadata metadata = this.metadataSupplier.get();

            Entity entityA = this.entitySupplier.get();
            String entityAId = String.format("%s.%s", entityA.getLogicalId(), context);
            Set<String> relationCategories = this.generationContext.getContextStatistics().getEntityRelationCategories().get(entityA.getCategory());

            int numOutRelations = this.numOutRelationsSupplier.get();
            List<Relation> outRelations = new ArrayList<>();
            while(outRelations.size() < numOutRelations) {
                Entity entityB = this.entitySupplier.get();
                String entityBId = String.format("%s.%s", entityB.getLogicalId(), context);

                Set<String> relationCategoriesB = this.generationContext.getContextStatistics().getEntityRelationCategories().get(entityB.getCategory());
                String category = this.categorySupplier.get();

                if (relationCategories.contains(category) && relationCategoriesB.contains(category)) {
                    outRelations.add(new Relation(context, category, entityAId, entityA.getCategory(), entityBId, entityB.getCategory()));
                }
            }

            int numInRelations = this.numInRelationsSupplier.get();
            List<Relation> inRelations = new ArrayList<>();
            while(outRelations.size() < numOutRelations) {
                Entity entityB = this.entitySupplier.get();
                String entityBId = String.format("%s.%s", entityB.getLogicalId(), context);

                Set<String> relationCategoriesB = this.generationContext.getContextStatistics().getEntityRelationCategories().get(entityB.getCategory());
                String category = this.categorySupplier.get();

                if (relationCategories.contains(category) && relationCategoriesB.contains(category)) {
                    outRelations.add(new Relation(context, category, entityAId, entityA.getCategory(), entityBId, entityB.getCategory()));
                }
            }

            entities.addAll(Stream.ofAll(outRelations)
                    .flatMap(relation -> {
                        String relationId = relationIdSupplier.get();

                        return Stream.of(new ElasticDocument<KnowledgeEntityBase>(
                                this.generationContext.getElasticConfiguration().getWriteSchema().getRelationIndex(),
                                "pge",
                                relationId,
                                null,
                                new Relation(
                                        context,
                                        relation.getCategory(),
                                        relation.getEntityAId(),
                                        relation.getEntityACategory(),
                                        relation.getEntityBId(),
                                        relation.getEntityBCategory(),
                                        metadata)),
                                new ElasticDocument<KnowledgeEntityBase>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex(),
                                        "pge",
                                        String.format("%s.out", relationId),
                                        entityA.getLogicalId(),
                                        new Erelation(
                                                relationId,
                                                context,
                                                relation.getCategory(),
                                                relation.getEntityAId(),
                                                relation.getEntityACategory(),
                                                relation.getEntityBId(),
                                                relation.getEntityBCategory(),
                                                "out",
                                                metadata)),
                                new ElasticDocument<KnowledgeEntityBase>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getRelationIndex(),
                                        "pge",
                                        String.format("%s.in", relationId),
                                        relation.getEntityBId().split("\\.")[0],
                                        new Erelation(
                                                relationId,
                                                context,
                                                relation.getCategory(),
                                                relation.getEntityBId(),
                                                relation.getEntityBCategory(),
                                                relation.getEntityAId(),
                                                relation.getEntityACategory(),
                                                "in",
                                                metadata)));
                    }).toJavaList());

            entities.addAll(Stream.ofAll(inRelations)
                    .flatMap(relation -> {
                        String relationId = relationIdSupplier.get();

                        return Stream.of(new ElasticDocument<KnowledgeEntityBase>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getRelationIndex(),
                                        "pge",
                                        relationId,
                                        null,
                                        new Relation(
                                                context,
                                                relation.getCategory(),
                                                relation.getEntityBId(),
                                                relation.getEntityBCategory(),
                                                relation.getEntityAId(),
                                                relation.getEntityACategory(),
                                                metadata)),
                                new ElasticDocument<KnowledgeEntityBase>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex(),
                                        "pge",
                                        String.format("%s.out", relationId),
                                        relation.getEntityBId().split("\\.")[0],
                                        new Erelation(
                                                relationId,
                                                context,
                                                relation.getCategory(),
                                                relation.getEntityBId(),
                                                relation.getEntityBCategory(),
                                                relation.getEntityAId(),
                                                relation.getEntityACategory(),
                                                "out",
                                                metadata)),
                                new ElasticDocument<KnowledgeEntityBase>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getRelationIndex(),
                                        "pge",
                                        String.format("%s.in", relationId),
                                        entityA.getLogicalId(),
                                        new Erelation(
                                                relationId,
                                                context,
                                                relation.getCategory(),
                                                relation.getEntityAId(),
                                                relation.getEntityACategory(),
                                                relation.getEntityBId(),
                                                relation.getEntityBCategory(),
                                                "in",
                                                metadata)));
                    }).toJavaList());

            this.numGenerated += outRelations.size() + inRelations.size();
        }

        return entities;
    }
    //endregion

    //region Fields
    private Client client;
    private GenerationContext generationContext;

    private Supplier<String> relationIdSupplier;
    private Supplier<String> categorySupplier;
    private Supplier<Entity> entitySupplier;
    private Supplier<KnowledgeEntityBase.Metadata> metadataSupplier;

    private Supplier<Integer> numOutRelationsSupplier;
    private Supplier<Integer> numInRelationsSupplier;

    private int numGenerated;
    private int numToGenerate;
    //endregion
}
