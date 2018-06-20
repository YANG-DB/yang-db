package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.unipop.controller.search.DefaultSearchOrderProvider;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;

public class SchemaKnowledgeGraphContextStatisticsProvider implements KnowledgeGraphContextStatisticsProvider {
    //region Constructors
    public SchemaKnowledgeGraphContextStatisticsProvider(Client client, LightSchema schema) {
        this.client = client;
        this.schema = schema;
    }
    //endregion

    //region KnowledgeGraphContextStatisticsProvider Implementation
    @Override
    public ContextStatistics getContextStatistics(String context) {
        ContextStatistics contextStatistics = new ContextStatistics();

        fillEntityCategoriesAndReferencesContextStatistics(contextStatistics, context);
        fillEntityFieldValueAndReferencesContextStatistics(contextStatistics, context);
        fillEntityRelationsContextStatistics(contextStatistics, context);
        fillRelationCategoriesAndReferencesContextStatistics(contextStatistics, context);
        fillRelationFieldValueAndReferencesContextStatistics(contextStatistics, context);
        fillInsightEntitiesAndReferencesContextStatistics(contextStatistics, context);

        return contextStatistics;
    }
    //endregion

    //region Private Methods
    private void fillEntityCategoriesAndReferencesContextStatistics(ContextStatistics contextStatistics, String context) {
        List<SearchHit> hits = Stream.ofAll(
                new SearchHitScrollIterable(
                        this.client,
                        client.prepareSearch().setIndices(this.schema.getEntityIndex())
                                .setQuery(boolQuery().filter(boolQuery()
                                        .must(termQuery("type", "entity"))
                                        .must(termQuery("context", context))
                                        .mustNot(existsQuery("deleteTime")))),
                        new DefaultSearchOrderProvider().build(null),
                        1000000000, 1000, 60000))
                .toJavaList();

        contextStatistics.setEntityCategories(
                Stream.ofAll(hits)
                        .groupBy(hit -> (String) hit.sourceAsMap().get("category"))
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));

        contextStatistics.setEntityReferenceCounts(
            Stream.ofAll(hits)
                .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.sourceAsMap().get("refs")).size())
                .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));
    }

    private void fillEntityFieldValueAndReferencesContextStatistics(ContextStatistics contextStatistics, String context) {
        List<SearchHit> hits = Stream.ofAll(
                new SearchHitScrollIterable(
                        this.client,
                        client.prepareSearch().setIndices(this.schema.getEntityIndex())
                                .setQuery(boolQuery().filter(boolQuery()
                                        .must(termQuery("type", "e.value"))
                                        .must(termsQuery("context", context))
                                        .mustNot(existsQuery("deleteTime")))),
                        new DefaultSearchOrderProvider().build(null),
                        1000000000, 1000, 60000))
                .toJavaList();

        contextStatistics.setEntityValueCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> (String)hit.sourceAsMap().get("fieldId"))
                        .map(grouping ->
                                new Tuple2<>(grouping._1(),
                                        grouping._2()
                                                .groupBy(hit -> (String)hit.sourceAsMap().get("logicalId"))
                                                .map(grouping1 -> grouping1._2().size())
                                                .groupBy(numValues -> numValues)
                                                .toJavaMap(grouping1 -> new Tuple2<>(grouping1._1(), grouping1._2().size()))))
                        .toJavaMap(tuple -> tuple));

        contextStatistics.setEntityValueReferenceCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.sourceAsMap().get("refs")).size())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));

        List<String> logicalIds = Stream.ofAll(hits).map(hit -> (String)hit.sourceAsMap().get("logicalId")).distinct().toJavaList();
        hits = Stream.ofAll(
                new SearchHitScrollIterable(
                        this.client,
                        client.prepareSearch().setIndices(this.schema.getEntityIndex())
                                .setQuery(boolQuery().filter(boolQuery()
                                        .must(termQuery("type", "e.value"))
                                        .must(termsQuery("context", "global"))
                                        .must(termsQuery("logicalId", logicalIds))
                                        .mustNot(existsQuery("deleteTime")))),
                        new DefaultSearchOrderProvider().build(null),
                        1000000000, 1000, 60000))
                .toJavaList();

        contextStatistics.setEntityGlobalValueCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> (String)hit.sourceAsMap().get("fieldId"))
                        .map(grouping ->
                                new Tuple2<>(grouping._1(),
                                        grouping._2()
                                                .groupBy(hit -> (String)hit.sourceAsMap().get("logicalId"))
                                                .map(grouping1 -> grouping1._2().size())
                                                .groupBy(numValues -> numValues)
                                                .toJavaMap(grouping1 -> new Tuple2<>(grouping1._1(), grouping1._2().size()))))
                        .toJavaMap(tuple -> tuple));

        contextStatistics.setEntityGlobalValueReferenceCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.sourceAsMap().get("refs")).size())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));
    }

    private void fillEntityRelationsContextStatistics(ContextStatistics contextStatistics, String context) {
        List<SearchHit> hits = Stream.ofAll(
                new SearchHitScrollIterable(
                        this.client,
                        client.prepareSearch().setIndices(this.schema.getEntityIndex())
                                .setQuery(boolQuery().filter(boolQuery()
                                        .must(termQuery("type", "e.relation"))
                                        .must(termsQuery("context", context))
                                        .mustNot(existsQuery("deleteTime")))),
                        new DefaultSearchOrderProvider().build(null),
                        1000000000, 1000, 60000))
                .toJavaList();

        contextStatistics.setEntityRelationCounts(
                Stream.ofAll(hits)
                    .groupBy(hit -> (String)hit.sourceAsMap().get("direction"))
                    .map(grouping -> new Tuple2<>(grouping._1(),
                            grouping._2()
                                    .groupBy(hit -> (String)hit.sourceAsMap().get("logicalId"))
                                    .map(grouping1 -> grouping1._2().size())
                                    .groupBy(numValues -> numValues)
                                    .toJavaMap(grouping1 -> new Tuple2<>(grouping1._1(), grouping1._2().size()))))
                    .toJavaMap(tuple -> tuple));
    }

    private void fillRelationCategoriesAndReferencesContextStatistics(ContextStatistics contextStatistics, String context) {
        List<SearchHit> hits = Stream.ofAll(
                new SearchHitScrollIterable(
                        this.client,
                        client.prepareSearch().setIndices(this.schema.getRelationIndex())
                                .setQuery(boolQuery().filter(boolQuery()
                                        .must(termQuery("type", "relation"))
                                        .must(termQuery("context", context))
                                        .mustNot(existsQuery("deleteTime")))),
                        new DefaultSearchOrderProvider().build(null),
                        1000000000, 1000, 60000))
                .toJavaList();

        contextStatistics.setRelationCategories(
                Stream.ofAll(hits)
                        .groupBy(hit -> (String) hit.sourceAsMap().get("category"))
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));

        contextStatistics.setRelationReferenceCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.sourceAsMap().get("refs")).size())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));
    }

    private void fillRelationFieldValueAndReferencesContextStatistics(ContextStatistics contextStatistics, String context) {
        List<SearchHit> hits = Stream.ofAll(
                new SearchHitScrollIterable(
                        this.client,
                        client.prepareSearch().setIndices(this.schema.getRelationIndex())
                                .setQuery(boolQuery().filter(boolQuery()
                                        .must(termQuery("type", "r.value"))
                                        .must(termsQuery("context", context))
                                        .mustNot(existsQuery("deleteTime")))),
                        new DefaultSearchOrderProvider().build(null),
                        1000000000, 1000, 60000))
                .toJavaList();

        contextStatistics.setRelationValueCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> (String)hit.sourceAsMap().get("fieldId"))
                        .map(grouping ->
                                new Tuple2<>(grouping._1(),
                                        grouping._2()
                                                .groupBy(hit -> (String)hit.sourceAsMap().get("relationId"))
                                                .map(grouping1 -> grouping1._2().size())
                                                .groupBy(numValues -> numValues)
                                                .toJavaMap(grouping1 -> new Tuple2<>(grouping1._1(), grouping1._2().size()))))
                        .toJavaMap(tuple -> tuple));

        contextStatistics.setRelationValueReferenceCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.sourceAsMap().get("refs")).size())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));
    }

    private void fillInsightEntitiesAndReferencesContextStatistics(ContextStatistics contextStatistics, String context) {
        List<SearchHit> hits = Stream.ofAll(
                new SearchHitScrollIterable(
                        this.client,
                        client.prepareSearch().setIndices(this.schema.getInsightIndex())
                                .setQuery(boolQuery().filter(boolQuery()
                                        .must(termQuery("type", "insight"))
                                        .must(termQuery("context", context))
                                        .mustNot(existsQuery("deleteTime")))),
                        new DefaultSearchOrderProvider().build(null),
                        1000000000, 1000, 60000))
                .toJavaList();

        contextStatistics.setInsightEntityCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.sourceAsMap().get("entityIds")).size())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));

        contextStatistics.setInsightReferenceCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.sourceAsMap().get("refs")).size())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));
    }
    //endregion

    //region Fields
    private Client client;
    private LightSchema schema;
    //endregion
}
