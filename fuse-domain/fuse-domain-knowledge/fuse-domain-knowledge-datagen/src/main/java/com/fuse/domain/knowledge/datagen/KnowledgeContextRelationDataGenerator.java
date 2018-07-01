package com.fuse.domain.knowledge.datagen;

import com.fuse.domain.knowledge.datagen.model.Erelation;
import com.fuse.domain.knowledge.datagen.model.KnowledgeEntityBase;
import com.fuse.domain.knowledge.datagen.model.Relation;
import com.kayhut.fuse.unipop.controller.search.DefaultSearchOrderProvider;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.*;
import java.util.function.Supplier;

import static org.elasticsearch.index.query.QueryBuilders.*;

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
            Supplier<String> logicalIdSupplier,
            Supplier<Integer> numOutRelationsSupplier,
            Supplier<Integer> numInRelationsSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier) {
        this.client = client;
        this.generationContext = generationContext;

        this.relationIdSupplier = relationIdSupplier;
        this.categorySupplier = categorySupplier;
        this.logicalIdSupplier = logicalIdSupplier;
        this.metadataSupplier = metadataSupplier;

        this.numOutRelationsSupplier = numOutRelationsSupplier;
        this.numInRelationsSupplier = numInRelationsSupplier;

        this.numToGenerate = (int) Math.floor(
                Stream.ofAll(this.generationContext.getContextStatistics().getRelationCategories().values()).sum().intValue() *
                        this.generationContext.getContextGenerationConfiguration().getScaleFactor());

        this.entityCategories = new HashMap<>();
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

            String logicalIdA = this.logicalIdSupplier.get();
            int numOutRelations = this.numOutRelationsSupplier.get();
            List<String> logicalIdsBOut = Stream.fill(numOutRelations, this.logicalIdSupplier).filter(id -> !id.equals(logicalIdA)).toJavaList();
            int numInRelations = this.numInRelationsSupplier.get();
            List<String> logicalIdsBIn = Stream.fill(numInRelations, this.logicalIdSupplier).filter(id -> !id.equals(logicalIdA)).toJavaList();

            List<String> unknownLogicalIds =
                    Stream.of(logicalIdA).appendAll(logicalIdsBOut).appendAll(logicalIdsBIn).distinct()
                            .filter(id -> !this.entityCategories.containsKey(id))
                            .toJavaList();

            this.entityCategories.putAll(Stream.ofAll(new SearchHitScrollIterable(
                    client,
                    client.prepareSearch().setIndices(this.generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex())
                            .setQuery(boolQuery().filter(boolQuery()
                                    .must(termQuery("type", "entity"))
                                    .must(termQuery("context", context))
                                    .must(termsQuery("logicalId", unknownLogicalIds))
                                    .mustNot(existsQuery("deleteTime"))))
                            .setFetchSource(new String[]{"category", "logicalId"}, null),
                    new DefaultSearchOrderProvider().build(null),
                    unknownLogicalIds.size(), 1000, 60000))
                    .toJavaMap(hit -> new Tuple2<>(
                            (String) hit.sourceAsMap().get("logicalId"),
                            (String) hit.sourceAsMap().get("category"))));

            entities.addAll(Stream.ofAll(logicalIdsBOut)
                    .flatMap(logicalIdB -> {
                        String relationId = relationIdSupplier.get();
                        String category = this.categorySupplier.get();

                        return Stream.of(new ElasticDocument<KnowledgeEntityBase>(
                                this.generationContext.getElasticConfiguration().getWriteSchema().getRelationIndex(),
                                "pge",
                                relationId,
                                null,
                                new Relation(
                                        context,
                                        category,
                                        String.format("%s.%s", logicalIdA, context),
                                        this.entityCategories.get(logicalIdA),
                                        String.format("%s.%s", logicalIdB, context),
                                        this.entityCategories.get(logicalIdB),
                                        metadata)),
                                new ElasticDocument<KnowledgeEntityBase>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex(),
                                        "pge",
                                        String.format("%s.out", relationId),
                                        logicalIdA,
                                        new Erelation(
                                                relationId,
                                                context,
                                                category,
                                                String.format("%s.%s", logicalIdA, context),
                                                this.entityCategories.get(logicalIdA),
                                                String.format("%s.%s", logicalIdB, context),
                                                this.entityCategories.get(logicalIdB),
                                                "out",
                                                metadata)),
                                new ElasticDocument<KnowledgeEntityBase>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getRelationIndex(),
                                        "pge",
                                        String.format("%s.in", relationId),
                                        logicalIdB,
                                        new Erelation(
                                                relationId,
                                                context,
                                                category,
                                                String.format("%s.%s", logicalIdB, context),
                                                this.entityCategories.get(logicalIdB),
                                                String.format("%s.%s", logicalIdA, context),
                                                this.entityCategories.get(logicalIdA),
                                                "in",
                                                metadata)));
                    }).toJavaList());

            entities.addAll(Stream.ofAll(logicalIdsBIn)
                    .flatMap(logicalIdB -> {
                        String relationId = relationIdSupplier.get();
                        String category = this.categorySupplier.get();

                        return Stream.of(new ElasticDocument<KnowledgeEntityBase>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getRelationIndex(),
                                        "pge",
                                        relationId,
                                        null,
                                        new Relation(
                                                context,
                                                category,
                                                String.format("%s.%s", logicalIdB, context),
                                                this.entityCategories.get(logicalIdB),
                                                String.format("%s.%s", logicalIdA, context),
                                                this.entityCategories.get(logicalIdA),
                                                metadata)),
                                new ElasticDocument<KnowledgeEntityBase>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getEntityIndex(),
                                        "pge",
                                        String.format("%s.out", relationId),
                                        logicalIdB,
                                        new Erelation(
                                                relationId,
                                                context,
                                                category,
                                                String.format("%s.%s", logicalIdB, context),
                                                this.entityCategories.get(logicalIdB),
                                                String.format("%s.%s", logicalIdA, context),
                                                this.entityCategories.get(logicalIdA),
                                                "out",
                                                metadata)),
                                new ElasticDocument<KnowledgeEntityBase>(
                                        this.generationContext.getElasticConfiguration().getWriteSchema().getRelationIndex(),
                                        "pge",
                                        String.format("%s.in", relationId),
                                        logicalIdA,
                                        new Erelation(
                                                relationId,
                                                context,
                                                category,
                                                String.format("%s.%s", logicalIdA, context),
                                                this.entityCategories.get(logicalIdA),
                                                String.format("%s.%s", logicalIdB, context),
                                                this.entityCategories.get(logicalIdB),
                                                "in",
                                                metadata)));
                    }).toJavaList());

            this.numGenerated += logicalIdsBIn.size() + logicalIdsBOut.size();
        }

        return entities;
    }
    //endregion

    //region Fields
    private Client client;
    private GenerationContext generationContext;

    private Supplier<String> relationIdSupplier;
    private Supplier<String> categorySupplier;
    private Supplier<String> logicalIdSupplier;
    private Supplier<KnowledgeEntityBase.Metadata> metadataSupplier;

    private Supplier<Integer> numOutRelationsSupplier;
    private Supplier<Integer> numInRelationsSupplier;

    private int numGenerated;
    private int numToGenerate;

    private Map<String, String> entityCategories;
    //endregion
}
