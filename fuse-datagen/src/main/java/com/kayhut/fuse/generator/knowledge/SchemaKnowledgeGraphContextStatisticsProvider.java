package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.unipop.controller.search.DefaultSearchOrderProvider;
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

        /*contextStatistics.setEntityReferenceCounts(
            Stream.ofAll(hits)
                .groupBy(hit -> hit.)
        );*/
    }
    //endregion

    //region Fields
    private Client client;
    private LightSchema schema;
    //endregion
}
