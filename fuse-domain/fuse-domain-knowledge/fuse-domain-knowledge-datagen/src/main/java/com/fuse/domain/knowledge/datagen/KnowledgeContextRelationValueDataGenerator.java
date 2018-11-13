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

import com.fuse.domain.knowledge.datagen.model.KnowledgeEntityBase;
import com.fuse.domain.knowledge.datagen.model.RvalueDate;
import com.fuse.domain.knowledge.datagen.model.RvalueInt;
import com.fuse.domain.knowledge.datagen.model.RvalueString;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/27/2018.
 */
public class KnowledgeContextRelationValueDataGenerator implements KnowledgeGraphGenerator<Object> {
    //region Constructors
    KnowledgeContextRelationValueDataGenerator(
            Client client,
            GenerationContext generationContext,
            String fieldId,
            String context,
            Supplier<String> relationValueIdSupplier,
            Supplier<String> relationIdSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier,
            Supplier<Integer> fieldNumValuesSupplier,
            Supplier fieldValuesSupplier) {
        this.client = client;
        this.generationContext = generationContext;

        this.fieldId = fieldId;
        this.context = context;

        this.relationValueIdSupplier = relationValueIdSupplier;
        this.relationIdSupplier = relationIdSupplier;
        this.metadataSupplier = metadataSupplier;

        this.fieldNumValuesSupplier = fieldNumValuesSupplier;
        this.fieldValuesSupplier = fieldValuesSupplier;
    }
    //endregion

    //region KnowledgeGraphGenerator Implementation
    @Override
    public Iterable<ElasticDocument<KnowledgeEntityBase>> generate(Object o) {
        List<ElasticDocument<KnowledgeEntityBase>> rvalues = new ArrayList<>();

        while(rvalues.size() < 1000) {
            try {
                String relationId = this.relationIdSupplier.get();
                String fieldType = this.generationContext.getContextStatistics().getEntityFieldTypes().get(this.fieldId);

                rvalues.addAll(
                        Stream.fill(
                                this.fieldNumValuesSupplier.get(),
                                () -> new ElasticDocument<>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getRelationIndex(),
                                        "pge",
                                        this.relationValueIdSupplier.get(),
                                        relationId,
                                        createValue(relationId, this.context, fieldId, fieldType)))
                                .toJavaList());
            } catch (NoSuchElementException ex) {
                break;
            }
        }

        return rvalues;
    }
    //endregion

    //region Private Methods
    private KnowledgeEntityBase createValue(String relationId, String context, String fieldId, String fieldType) {
        switch (fieldType) {
            case "stringValue":
                return new RvalueString(
                        relationId,
                        context,
                        fieldId,
                        (String)this.fieldValuesSupplier.get(),
                        metadataSupplier.get());
            case "intValue":
                return new RvalueInt(
                        relationId,
                        context,
                        fieldId,
                        (int)this.fieldValuesSupplier.get(),
                        metadataSupplier.get());
            case "dateValue":
                return new RvalueDate(
                        relationId,
                        context,
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

    private Supplier<String> relationValueIdSupplier;
    private Supplier<String> relationIdSupplier;
    private Supplier<KnowledgeEntityBase.Metadata> metadataSupplier;

    private String fieldId;
    private String context;

    private Supplier<Integer> fieldNumValuesSupplier;
    private Supplier fieldValuesSupplier;
    //endregion
}
