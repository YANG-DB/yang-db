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
            Supplier<String> entityValueIdSupplier,
            Supplier<String> logicalIdSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier,
            Map<String, Supplier<Integer>> fieldNumValuesSuppliers,
            Map<String, Supplier> fieldValuesSuppliers) {
        this.client = client;
        this.generationContext = generationContext;

        this.entityValueIdSupplier = entityValueIdSupplier;
        this.logicalIdSupplier = logicalIdSupplier;
        this.metadataSupplier = metadataSupplier;

        this.fieldNumValuesSuppliers = fieldNumValuesSuppliers;
        this.fieldValuesSuppliers = fieldValuesSuppliers;
    }
    //endregion

    //region KnowledgeGraphGenerator Implementation
    @Override
    public Iterable<ElasticDocument<KnowledgeEntityBase>> generate(Object o) {
        List<ElasticDocument<KnowledgeEntityBase>> evalues = new ArrayList<>();

        while(evalues.size() < 1000) {
            try {
                String logicalId = this.logicalIdSupplier.get();
                String context = this.generationContext.getContextGenerationConfiguration().getToContext();
                String entityId = String.format("%s.%s", logicalId, context);

                evalues.addAll(
                        Stream.ofAll(this.generationContext.getContextStatistics().getEntityFieldTypes().entrySet())
                        .flatMap(fieldType -> Stream.fill(this.fieldNumValuesSuppliers.get(fieldType.getKey()).get(),
                                () -> new ElasticDocument<>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex(),
                                        "pge",
                                        this.entityValueIdSupplier.get(),
                                        logicalId,
                                        createValue(logicalId, context, entityId, fieldType.getKey(), fieldType.getValue()))))
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
                        (String)this.fieldValuesSuppliers.get(fieldId).get(),
                        metadataSupplier.get());
            case "intValue":
                return new EvalueInt(
                        logicalId,
                        context,
                        entityId,
                        fieldId,
                        (int)this.fieldValuesSuppliers.get(fieldId).get(),
                        metadataSupplier.get());
            case "dateValue":
                return new EvalueDate(
                        logicalId,
                        context,
                        entityId,
                        fieldId,
                        (Date)this.fieldValuesSuppliers.get(fieldId).get(),
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

    private Map<String, Supplier<Integer>> fieldNumValuesSuppliers;
    private Map<String, Supplier> fieldValuesSuppliers;
    //endregion
}
