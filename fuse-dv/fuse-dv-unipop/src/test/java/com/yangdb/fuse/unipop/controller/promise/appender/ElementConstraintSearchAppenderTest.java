package com.yangdb.fuse.unipop.controller.promise.appender;

import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.controller.promise.context.PromiseElementControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.promise.Constraint;
import com.yangdb.fuse.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import com.yangdb.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.unipop.process.predicate.CountFilterP;
import org.unipop.query.StepDescriptor;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;

/**
 * Created by lior.perry on 29/03/2017.
 */
public class ElementConstraintSearchAppenderTest {
    @Test
    public void testNoConstraint() {
        SearchBuilder searchBuilder = new SearchBuilder();

        ElementConstraintSearchAppender appender = new ElementConstraintSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                null,
                new StepDescriptor(mock(Step.class)),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                EmptyGraphElementSchemaProvider.instance,
                ElementType.vertex,
                0));

        Assert.assertTrue(!appendResult);
        Assert.assertTrue(searchBuilder.getQueryBuilder().getQuery() == null);
    }

    @Test
    public void testSimpleConstraint() throws JSONException {

        SearchBuilder searchBuilder = new SearchBuilder();

        ElementConstraintSearchAppender appender = new ElementConstraintSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                null,
                new StepDescriptor(mock(Step.class)),
                Collections.emptyList(),
                Optional.of(Constraint.by(__.has(T.label, "dragon"))),
                Collections.emptyList(),
                EmptyGraphElementSchemaProvider.instance,
                ElementType.vertex,
                0));

        Assert.assertTrue(appendResult);
        JSONAssert.assertEquals(
                "{\n" +
                        "  \"bool\" : {\n" +
                        "    \"filter\" : [\n" +
                        "      {\n" +
                        "        \"bool\" : {\n" +
                        "          \"must\" : [\n" +
                        "            {\n" +
                        "              \"term\" : {\n" +
                        "                \"type\" : {\n" +
                        "                  \"value\" : \"dragon\",\n" +
                        "                  \"boost\" : 1.0\n" +
                        "                }\n" +
                        "              }\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"adjust_pure_negative\" : true,\n" +
                        "          \"boost\" : 1.0\n" +
                        "        }\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"adjust_pure_negative\" : true,\n" +
                        "    \"boost\" : 1.0\n" +
                        "  }\n" +
                        "}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    public void testSimpleCountConstraint() throws JSONException {

        Traversal dragonTraversal = __.has(GlobalConstants.EdgeSchema.SOURCE_ID, CountFilterP.gt(12));

        SearchBuilder searchBuilder = new SearchBuilder();

        ElementConstraintSearchAppender appender = new ElementConstraintSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                null,
                new StepDescriptor(mock(Step.class)),
                Collections.emptyList(),
                Optional.of(Constraint.by(dragonTraversal)),
                Collections.emptyList(),
                EmptyGraphElementSchemaProvider.instance,
                ElementType.vertex,
                0));

        Assert.assertTrue(appendResult);
        JSONAssert.assertEquals(
                "{\n" +
                        "  \"bool\" : {\n" +
                        "    \"filter\" : [\n" +
                        "      {\n" +
                        "        \"bool\" : {\n" +
                        "          \"adjust_pure_negative\" : true,\n" +
                        "          \"boost\" : 1.0\n" +
                        "        }\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"adjust_pure_negative\" : true,\n" +
                        "    \"boost\" : 1.0\n" +
                        "  }\n" +
                        "}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

        Assert.assertEquals(
                "[{\"entityA.id\":{\"terms\":{\"field\":\"entityA.id\",\"size\":10,\"min_doc_count\":1,\"shard_min_doc_count\":0,\"show_term_doc_count_error\":false,\"order\":[{\"_count\":\"desc\"},{\"_key\":\"asc\"}]},\"aggregations\":{\"entityA.id\":{\"bucket_selector\":{\"buckets_path\":{\"entityA_id_Count\":\"_count\"},\"script\":{\"source\":\"def a=params.entityA_id_Count;a>12\",\"lang\":\"painless\"},\"gap_policy\":\"skip\"}}}}}]",
                searchBuilder.getAggregationBuilder().getAggregations().toString());
    }

    @Test
    public void testComplexConstraint() throws JSONException {
        SearchBuilder searchBuilder = new SearchBuilder();

        ElementConstraintSearchAppender appender = new ElementConstraintSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                null,
                new StepDescriptor(mock(Step.class)),
                Collections.emptyList(),
                Optional.of(Constraint.by(__.and(__.has(T.label, "dragon"), __.has("name", "Drogar")))),
                Collections.emptyList(),
                EmptyGraphElementSchemaProvider.instance,
                ElementType.vertex,
                0));

        Assert.assertTrue(appendResult);
        JSONAssert.assertEquals(
                "{\n" +
                        "  \"bool\" : {\n" +
                        "    \"filter\" : [\n" +
                        "      {\n" +
                        "        \"bool\" : {\n" +
                        "          \"must\" : [\n" +
                        "            {\n" +
                        "              \"bool\" : {\n" +
                        "                \"filter\" : [\n" +
                        "                  {\n" +
                        "                    \"bool\" : {\n" +
                        "                      \"must\" : [\n" +
                        "                        {\n" +
                        "                          \"term\" : {\n" +
                        "                            \"type\" : {\n" +
                        "                              \"value\" : \"dragon\",\n" +
                        "                              \"boost\" : 1.0\n" +
                        "                            }\n" +
                        "                          }\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                          \"term\" : {\n" +
                        "                            \"name\" : {\n" +
                        "                              \"value\" : \"Drogar\",\n" +
                        "                              \"boost\" : 1.0\n" +
                        "                            }\n" +
                        "                          }\n" +
                        "                        }\n" +
                        "                      ],\n" +
                        "                      \"adjust_pure_negative\" : true,\n" +
                        "                      \"boost\" : 1.0\n" +
                        "                    }\n" +
                        "                  }\n" +
                        "                ],\n" +
                        "                \"adjust_pure_negative\" : true,\n" +
                        "                \"boost\" : 1.0\n" +
                        "              }\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"adjust_pure_negative\" : true,\n" +
                        "          \"boost\" : 1.0\n" +
                        "        }\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"adjust_pure_negative\" : true,\n" +
                        "    \"boost\" : 1.0\n" +
                        "  }\n" +
                        "}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);
    }
}
