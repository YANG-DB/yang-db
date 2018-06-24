package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.generator.knowledge.model.EvalueDate;
import com.kayhut.fuse.generator.knowledge.model.EvalueInt;
import com.kayhut.fuse.generator.knowledge.model.EvalueString;
import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;
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
            Supplier<String> logicalIdSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier,
            Supplier<Integer> fieldNumValuesSupplier,
            Supplier fieldValuesSupplier) {
        this.client = client;
        this.generationContext = generationContext;

        this.fieldId = fieldId;
        this.context = context;

        this.entityValueIdSupplier = entityValueIdSupplier;
        this.logicalIdSupplier = logicalIdSupplier;
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
                String logicalId = this.logicalIdSupplier.get();
                String entityId = String.format("%s.%s", logicalId, this.context);
                String fieldType = this.generationContext.getContextStatistics().getEntityFieldTypes().get(this.fieldId);

                evalues.addAll(
                        Stream.fill(
                                this.fieldNumValuesSupplier.get(),
                                () -> new ElasticDocument<>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex(),
                                        "pge",
                                        this.entityValueIdSupplier.get(),
                                        logicalId,
                                        createValue(logicalId, this.context, entityId, fieldId, fieldType)))
                        .toJavaList());
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
    private Supplier<String> logicalIdSupplier;
    private Supplier<KnowledgeEntityBase.Metadata> metadataSupplier;

    private String fieldId;
    private String context;

    private Supplier<Integer> fieldNumValuesSupplier;
    private Supplier fieldValuesSupplier;
    //endregion
}
