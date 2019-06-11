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

import com.kayhut.fuse.unipop.controller.search.DefaultSearchOrderProvider;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        contextStatistics.setDistinctNumReferences(getDistintcNumReferences(context));

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
                        .groupBy(hit -> (String) hit.getSourceAsMap().get("category"))
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));

        contextStatistics.setEntityCategoryFields(Stream.ofAll(contextStatistics.getEntityCategories().keySet())
                .toJavaMap(category -> {
                    List<String> categoryLogicalIds = Stream.ofAll(new SearchHitScrollIterable(
                            this.client,
                            client.prepareSearch().setIndices(this.schema.getEntityIndex())
                                .setQuery(boolQuery().filter(boolQuery()
                                    .must(termQuery("type", "entity"))
                                    .must(termQuery("context", context))
                                    .mustNot(existsQuery("deleteTime"))))
                                .setFetchSource(new String[] { "logicalId" }, null),
                            new DefaultSearchOrderProvider().build(null),
                            1000000000, 1000, 60000))
                            .map(hit -> (String)hit.getSourceAsMap().get("logicalId"))
                            .distinct().toJavaList();

                    Set<String> categoryFields = Stream.ofAll(new SearchHitScrollIterable(
                            this.client,
                            client.prepareSearch().setIndices(this.schema.getEntityIndex())
                            .setQuery(boolQuery().filter(boolQuery()
                                .must(termQuery("type", "e.value"))
                                .must(termQuery("context", context))
                                .must(termsQuery("logicalId", categoryLogicalIds))
                                .mustNot(existsQuery("deleteTime"))))
                            .setFetchSource(new String[] { "fieldId" }, null),
                            new DefaultSearchOrderProvider().build(null),
                            1000000000, 1000, 60000))
                            .map(hit -> (String)hit.getSourceAsMap().get("fieldId"))
                            .toJavaSet();

                    categoryFields.add("title");
                    categoryFields.add("nicknames");

                    return new Tuple2<>(category, categoryFields);
                }));

        contextStatistics.setEntityRelationCategories(Stream.ofAll(contextStatistics.getEntityCategories().keySet())
                .toJavaMap(category -> {
                    List<String> categoryEntityIds = Stream.ofAll(new SearchHitScrollIterable(
                            this.client,
                            client.prepareSearch().setIndices(this.schema.getEntityIndex())
                                    .setQuery(boolQuery().filter(boolQuery()
                                            .must(termQuery("type", "entity"))
                                            .must(termQuery("context", context))
                                            .mustNot(existsQuery("deleteTime"))))
                                    .setFetchSource(new String[] { "logicalId" }, null),
                            new DefaultSearchOrderProvider().build(null),
                            1000000000, 1000, 60000))
                            .map(hit -> (String)hit.getSourceAsMap().get("logicalId"))
                            .distinct().map(logicalId -> logicalId + "." + context).toJavaList();

                    Set<String> relationCategories = Stream.ofAll(new SearchHitScrollIterable(
                            this.client,
                            client.prepareSearch().setIndices(this.schema.getEntityIndex())
                                    .setQuery(boolQuery().filter(boolQuery()
                                            .must(termQuery("type", "e.relation"))
                                            .must(termQuery("context", context))
                                            .must(termsQuery("entityAId", categoryEntityIds))
                                            .mustNot(existsQuery("deleteTime"))))
                                    .setFetchSource(new String[] { "category" }, null),
                            new DefaultSearchOrderProvider().build(null),
                            1000000000, 1000, 60000))
                            .map(hit -> (String)hit.getSourceAsMap().get("category"))
                            .toJavaSet();

                    return new Tuple2<>(category, relationCategories);
                }));

        contextStatistics.setEntityReferenceCounts(
            Stream.ofAll(hits)
                .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.getSourceAsMap().get("refs")).size())
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
                        .groupBy(hit -> (String)hit.getSourceAsMap().get("fieldId"))
                        .map(grouping ->
                                new Tuple2<>(grouping._1(),
                                        grouping._2()
                                                .groupBy(hit -> (String)hit.getSourceAsMap().get("logicalId"))
                                                .map(grouping1 -> grouping1._2().size())
                                                .groupBy(numValues -> numValues)
                                                .toJavaMap(grouping1 -> new Tuple2<>(grouping1._1(), grouping1._2().size()))))
                        .toJavaMap(tuple -> tuple));

        contextStatistics.setEntityValueReferenceCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.getSourceAsMap().get("refs")).size())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));

        contextStatistics.setEntityFieldTypes(
                Stream.ofAll(hits)
                    .groupBy(hit -> (String)hit.getSourceAsMap().get("fieldId"))
                        .toJavaMap(grouping -> {
                            SearchHit hit = grouping._2().get(0);
                            String type = null;
                            if (hit.getSourceAsMap().containsKey("stringValue")) {
                                type = "stringValue";
                            } else if (hit.getSourceAsMap().containsKey("intValue")) {
                                type = "intValue";
                            } else if (hit.getSourceAsMap().containsKey("dateValue")) {
                                type = "dateValue";
                            }

                            return new Tuple2(grouping._1(), type);
                        }));
        contextStatistics.getEntityFieldTypes().put("title", "stringValue");
        contextStatistics.getEntityFieldTypes().put("nicknames", "stringValue");

        // add zero field counts
        int numEntitiesInContext = Stream.ofAll(contextStatistics.getEntityCategories().values()).sum().intValue();
        Stream.ofAll(contextStatistics.getEntityFieldTypes().keySet())
                .map(fieldId -> new Tuple2<>(fieldId, contextStatistics.getEntityValueCounts().get(fieldId).size()))
                .forEach(tuple -> contextStatistics.getEntityValueCounts().get(tuple._1()).put(0, numEntitiesInContext - tuple._2()));

        List<String> logicalIds = Stream.ofAll(hits).map(hit -> (String)hit.getSourceAsMap().get("logicalId")).distinct().toJavaList();
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
                        .groupBy(hit -> (String)hit.getSourceAsMap().get("fieldId"))
                        .map(grouping ->
                                new Tuple2<>(grouping._1(),
                                        grouping._2()
                                                .groupBy(hit -> (String)hit.getSourceAsMap().get("logicalId"))
                                                .map(grouping1 -> grouping1._2().size())
                                                .groupBy(numValues -> numValues)
                                                .toJavaMap(grouping1 -> new Tuple2<>(grouping1._1(), grouping1._2().size()))))
                        .toJavaMap(tuple -> tuple));

        contextStatistics.setEntityGlobalValueReferenceCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.getSourceAsMap().get("refs")).size())
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
                    .groupBy(hit -> (String)hit.getSourceAsMap().get("direction"))
                    .map(grouping -> new Tuple2<>(grouping._1(),
                            grouping._2()
                                    .groupBy(hit -> (String)hit.getSourceAsMap().get("logicalId"))
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
                        .groupBy(hit -> (String) hit.getSourceAsMap().get("category"))
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));

        contextStatistics.setRelationCategoryFields(Stream.ofAll(contextStatistics.getRelationCategories().keySet())
                .toJavaMap(category -> {
                    List<String> categoryRelationIds = Stream.ofAll(new SearchHitScrollIterable(
                            this.client,
                            client.prepareSearch().setIndices(this.schema.getRelationIndex())
                                    .setQuery(boolQuery().filter(boolQuery()
                                            .must(termQuery("type", "relation"))
                                            .must(termQuery("context", context))
                                            .mustNot(existsQuery("deleteTime"))))
                                    .setFetchSource(false),
                            new DefaultSearchOrderProvider().build(null),
                            1000000000, 1000, 60000))
                            .map(SearchHit::getId)
                            .distinct().toJavaList();

                    Set<String> categoryFields = Stream.ofAll(new SearchHitScrollIterable(
                            this.client,
                            client.prepareSearch().setIndices(this.schema.getRelationIndex())
                                    .setQuery(boolQuery().filter(boolQuery()
                                            .must(termQuery("type", "r.value"))
                                            .must(termQuery("context", context))
                                            .must(termsQuery("relationId", categoryRelationIds))
                                            .mustNot(existsQuery("deleteTime"))))
                                    .setFetchSource(new String[] { "fieldId" }, null),
                            new DefaultSearchOrderProvider().build(null),
                            1000000000, 1000, 60000))
                            .map(hit -> (String)hit.getSourceAsMap().get("fieldId"))
                            .toJavaSet();

                    categoryFields.add("title");
                    categoryFields.add("nicknames");

                    return new Tuple2<>(category, categoryFields);
                }));

        contextStatistics.setRelationReferenceCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.getSourceAsMap().get("refs")).size())
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
                        .groupBy(hit -> (String)hit.getSourceAsMap().get("fieldId"))
                        .map(grouping ->
                                new Tuple2<>(grouping._1(),
                                        grouping._2()
                                                .groupBy(hit -> (String)hit.getSourceAsMap().get("relationId"))
                                                .map(grouping1 -> grouping1._2().size())
                                                .groupBy(numValues -> numValues)
                                                .toJavaMap(grouping1 -> new Tuple2<>(grouping1._1(), grouping1._2().size()))))
                        .toJavaMap(tuple -> tuple));

        contextStatistics.setRelationValueReferenceCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.getSourceAsMap().get("refs")).size())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));

        contextStatistics.setRelationFieldTypes(
                Stream.ofAll(hits)
                        .groupBy(hit -> (String)hit.getSourceAsMap().get("fieldId"))
                        .toJavaMap(grouping -> {
                            SearchHit hit = grouping._2().get(0);
                            String type = null;
                            if (hit.getSourceAsMap().containsKey("stringValue")) {
                                type = "stringValue";
                            } else if (hit.getSourceAsMap().containsKey("intValue")) {
                                type = "intValue";
                            } else if (hit.getSourceAsMap().containsKey("dateValue")) {
                                type = "dateValue";
                            }

                            return new Tuple2(grouping._1(), type);
                        }));
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
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.getSourceAsMap().get("entityIds")).size())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));

        contextStatistics.setInsightReferenceCounts(
                Stream.ofAll(hits)
                        .groupBy(hit -> CollectionUtil.listFromObjectValue(hit.getSourceAsMap().get("refs")).size())
                        .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().size())));
    }

    private long getDistintcNumReferences(String context) {
        SearchResponse response = this.client.prepareSearch()
                .setIndices(String.join(",", this.schema.getEntityIndex(), this.schema.getRelationIndex(), this.schema.getInsightIndex()))
                .setQuery(boolQuery().filter(boolQuery()
                        .must(termsQuery("type", Arrays.asList("entity", "e.value", "relation", "r.value", "insight")))
                        .must(termQuery("context", context))
                        .mustNot(existsQuery("deleteTime"))))
                .addAggregation(AggregationBuilders.cardinality("countDistinctRefs").field("refs").precisionThreshold(40000))
                .execute().actionGet();

        Cardinality cardinality = response.getAggregations().get("a");
        return cardinality.getValue();
    }
    //endregion

    //region Fields
    private Client client;
    private LightSchema schema;
    //endregion
}
