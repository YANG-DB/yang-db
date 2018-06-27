package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.generator.knowledge.model.*;
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
            Supplier<String> logicalIdSupplier,
            Supplier<Integer> numEntitiesSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier) {
        this.client = client;
        this.generationContext = generationContext;

        this.insightIdSupplier = insightIdSupplier;
        this.contentSupplier = contentSupplier;
        this.logicalIdSupplier = logicalIdSupplier;
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

            List<String> logicalIdsInInsight = Stream.fill(this.numEntitiesSupplier.get(), this.logicalIdSupplier).toJavaList();
            String insightId = this.insightIdSupplier.get();

            insights.add(new ElasticDocument<>(
                    generationContext.getElasticConfiguration().getWriteSchema().getInsightIndex(),
                    "pge",
                    insightId,
                    null,
                    new Insight(
                            context,
                            this.contentSupplier.get(),
                            Stream.of(logicalIdsInInsight).map(id -> String.format("%s.%s", id, context)).toJavaList(),
                            metadata)));

            insights.addAll(Stream.ofAll(logicalIdsInInsight)
                    .map(logicalId -> new ElasticDocument<KnowledgeEntityBase>(
                            generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex(),
                            "pge",
                            String.format("%s.%s", logicalId, insightId),
                            logicalId,
                            new Einsight(String.format("%s.%s", logicalId, context), insightId)))
                    .toJavaList());

            this.numGenerated += logicalIdsInInsight.size() + 1;
        }

        return insights;
    }
    //endregion

    //region Fields
    private Client client;
    private GenerationContext generationContext;

    private Supplier<String> insightIdSupplier;
    private Supplier<String> contentSupplier;
    private Supplier<String> logicalIdSupplier;
    private Supplier<Integer> numEntitiesSupplier;
    private Supplier<KnowledgeEntityBase.Metadata> metadataSupplier;

    private int numGenerated;
    private int numToGenerate;
    //endregion
}
