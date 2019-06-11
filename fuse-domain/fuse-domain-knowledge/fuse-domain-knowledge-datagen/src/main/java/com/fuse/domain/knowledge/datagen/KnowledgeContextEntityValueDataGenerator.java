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

import com.fuse.domain.knowledge.datagen.model.*;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;

import java.util.*;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/23/2018.
 */
public class KnowledgeContextEntityValueDataGenerator implements KnowledgeGraphGenerator<Object> {
    //region Constructors
    KnowledgeContextEntityValueDataGenerator(
            Client client,
            GenerationContext generationContext,
            String fieldId,
            String context,
            Supplier<String> entityValueIdSupplier,
            Supplier<Entity> entitySupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier,
            Supplier<Integer> fieldNumValuesSupplier,
            Supplier fieldValuesSupplier) {
        this.client = client;
        this.generationContext = generationContext;

        this.fieldId = fieldId;
        this.context = context;

        this.entityValueIdSupplier = entityValueIdSupplier;
        this.entitySupplier = entitySupplier;
        this.metadataSupplier = metadataSupplier;

        this.fieldNumValuesSupplier = fieldNumValuesSupplier;
        this.fieldValuesSupplier = fieldValuesSupplier;
    }
    //endregion

    //region KnowledgeGraphGenerator Implementation
    @Override
    public Iterable<ElasticDocument<KnowledgeEntityBase>> generate(Object o) {
        List<ElasticDocument<KnowledgeEntityBase>> evalues = new ArrayList<>();

        while(evalues.size() < 1000) {
            try {
                Entity entity = this.entitySupplier.get();
                Set<String> entityCategoryFields = this.generationContext.getContextStatistics().getEntityCategoryFields().get(entity.getCategory());

                if (entityCategoryFields != null && entityCategoryFields.contains(this.fieldId)) {
                    String entityId = String.format("%s.%s", entity.getLogicalId(), this.context);
                    String fieldType = this.generationContext.getContextStatistics().getEntityFieldTypes().get(this.fieldId);

                    evalues.addAll(
                            Stream.fill(
                                    this.fieldNumValuesSupplier.get(),
                                    () -> new ElasticDocument<>(
                                            this.generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex(),
                                            "pge",
                                            this.entityValueIdSupplier.get(),
                                            entity.getLogicalId(),
                                            createValue(entity.getLogicalId(), this.context, entityId, fieldId, fieldType)))
                                    .toJavaList());
                }
            } catch (NoSuchElementException ex) {
                break;
            }
        }

        return evalues;
    }
    //endregion

    //region Private Methods
    private KnowledgeEntityBase createValue(String logicalId, String context, String entityId, String fieldId, String fieldType) {
        switch (fieldType) {
            case "stringValue":
                return new EvalueString(
                        logicalId,
                        context,
                        entityId,
                        fieldId,
                        (String)this.fieldValuesSupplier.get(),
                        metadataSupplier.get());
            case "intValue":
                return new EvalueInt(
                        logicalId,
                        context,
                        entityId,
                        fieldId,
                        (int)this.fieldValuesSupplier.get(),
                        metadataSupplier.get());
            case "floatValue":
                return new EvalueFloat(
                        logicalId,
                        context,
                        entityId,
                        fieldId,
                        new Float((int) this.fieldValuesSupplier.get()).floatValue(),
                        metadataSupplier.get());
            case "longValue":
                return new EvalueLong(
                        logicalId,
                        context,
                        entityId,
                        fieldId,
                        new Long((int) this.fieldValuesSupplier.get()).longValue(),
                        metadataSupplier.get());
            case "dateValue":
                return new EvalueDate(
                        logicalId,
                        context,
                        entityId,
                        fieldId,
                        (Date)this.fieldValuesSupplier.get(),
                        metadataSupplier.get());
            default: throw new RuntimeException("unsupported field type: " + fieldType);
        }
    }
    //endregion

    //region Fields
    private Client client;
    private GenerationContext generationContext;

    private Supplier<String> entityValueIdSupplier;
    private Supplier<Entity> entitySupplier;
    private Supplier<KnowledgeEntityBase.Metadata> metadataSupplier;

    private String fieldId;
    private String context;

    private Supplier<Integer> fieldNumValuesSupplier;
    private Supplier fieldValuesSupplier;
    //endregion
}
