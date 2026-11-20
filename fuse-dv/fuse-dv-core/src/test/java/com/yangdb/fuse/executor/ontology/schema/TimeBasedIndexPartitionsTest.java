package com.yangdb.fuse.executor.ontology.schema;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Lists;
import com.yangdb.fuse.executor.ontology.schema.load.DocumentBuilder;
import com.yangdb.fuse.executor.ontology.schema.load.LoadUtils;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.Props;
import com.yangdb.fuse.unipop.controller.common.context.VertexControllerContext;
import com.yangdb.fuse.unipop.controller.promise.appender.PromiseEdgeIndexAppender;
import com.yangdb.fuse.unipop.controller.promise.context.PromiseVertexControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.unipop.query.StepDescriptor;

import java.util.*;

import static org.mockito.Mockito.mock;

/**
 * L4: time partition "values" are range buckets - each value is the inclusive upper bound of its bucket
 * (the first bucket is open below), compared numerically. 500/1000/1500/2000 with yyyy:
 * year <= 500 -> _500, 501..1000 -> _1000, 1001..1500 -> _1500, 1501..2000 -> _2000, after 2000 -> no bucket.
 */
public class TimeBasedIndexPartitionsTest {
    private static final String FIRE = "Fire";

    private static GraphElementSchemaProviderJsonFactory.TimeBasedIndexPartitions fire() {
        Props props = new Props(Arrays.asList("500", "1000", "1500", "2000"), "date", "idx_fire",
                "idx_fire_%s", "yyyy", new HashMap<>());
        return new GraphElementSchemaProviderJsonFactory.TimeBasedIndexPartitions(props);
    }

    private static Date utc(int year, int month, int day, int hour, int minute) {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(year, month - 1, day, hour, minute);
        return calendar.getTime();
    }

    @Test
    public void indicesAreOrderedNumerically() {
        Assert.assertEquals(Arrays.asList("idx_fire_500", "idx_fire_1000", "idx_fire_1500", "idx_fire_2000"),
                Lists.newArrayList(fire().getIndices()));
    }

    @Test
    public void dateGoesToTheBucketWhoseUpperBoundCoversIt() {
        Assert.assertEquals("idx_fire_500", fire().getIndexName(utc(100, 6, 1, 0, 0)));
        Assert.assertEquals("idx_fire_500", fire().getIndexName(utc(500, 12, 31, 23, 59)));
        Assert.assertEquals("idx_fire_1000", fire().getIndexName(utc(501, 1, 1, 0, 0)));
        Assert.assertEquals("idx_fire_1000", fire().getIndexName(utc(1000, 6, 15, 0, 0)));
        Assert.assertEquals("idx_fire_1000", fire().getIndexName(utc(1000, 12, 31, 23, 59)));
        Assert.assertEquals("idx_fire_1500", fire().getIndexName(utc(1001, 1, 1, 0, 0)));
        Assert.assertEquals("idx_fire_2000", fire().getIndexName(utc(1950, 6, 15, 0, 0)));
        Assert.assertEquals("idx_fire_2000", fire().getIndexName(utc(2000, 12, 31, 23, 59)));
    }

    @Test
    public void dateAfterTheLastBucketHasNoIndex() {
        Assert.assertNull(fire().getIndexName(utc(2001, 1, 1, 0, 0)));
        Assert.assertNull(fire().getIndexName(utc(2050, 6, 15, 0, 0)));
    }

    @Test
    public void loaderRoutesToTheBucket() throws Exception {
        PartitionResolver resolver = resolver();
        Assert.assertEquals("idx_fire_2000", LoadUtils.resolveIndex(resolver, fireDoc("1950-06-15 00:00:00.000")));
        Assert.assertEquals("idx_fire_1000", LoadUtils.resolveIndex(resolver, fireDoc("1000-06-15 00:00:00.000")));
        Assert.assertEquals("idx_fire_500", LoadUtils.resolveIndex(resolver, fireDoc("0300-06-15 00:00:00.000")));
    }

    @Test
    public void loaderFailsLoudlyOutsideEveryBucket() throws Exception {
        try {
            String index = LoadUtils.resolveIndex(resolver(), fireDoc("2050-06-15 00:00:00.000"));
            Assert.fail("expected a load failure, got index " + index);
        } catch (FuseError.FuseErrorException expected) {
            Assert.assertTrue(expected.getError().getErrorDescription(),
                    expected.getError().getErrorDescription().contains("2050"));
        }
    }

    @Test
    public void loaderFailsLoudlyWithoutAPartitionValue() throws Exception {
        try {
            String index = LoadUtils.resolveIndex(resolver(),
                    new DocumentBuilder(JsonNodeFactory.instance.objectNode(), "1", FIRE, Optional.empty(), Optional.empty()));
            Assert.fail("expected a load failure, got index " + index);
        } catch (FuseError.FuseErrorException expected) {
            // expected
        }
    }

    /**
     * the service wraps the schema in Cached -> PartitionFiltered -> Prefixed (ExecutorModule.bindRawSchema);
     * the wrappers must keep the time partitioning, or the loader falls back to the first index
     */
    @Test
    public void serviceSchemaWrappersKeepTimeBuckets() throws Exception {
        Assert.assertEquals("idx_fire_2000", LoadUtils.resolveIndex(serviceChain(""), fireDoc("1909-03-27 07:44:48.177")));
        Assert.assertEquals("p_idx_fire_2000", LoadUtils.resolveIndex(serviceChain("p_"), fireDoc("1909-03-27 07:44:48.177")));
        Assert.assertEquals("p_idx_fire_1000", LoadUtils.resolveIndex(serviceChain("p_"), fireDoc("1000-06-15 00:00:00.000")));
        try {
            LoadUtils.resolveIndex(serviceChain(""), fireDoc("2050-06-15 00:00:00.000"));
            Assert.fail("expected a load failure");
        } catch (FuseError.FuseErrorException expected) {
            // expected
        }
    }

    private static RawSchema serviceChain(String prefix) {
        GraphElementSchemaProvider provider = new GraphElementSchemaProvider.Impl(
                Collections.emptyList(),
                Collections.singletonList(new GraphEdgeSchema.Impl(FIRE, fire())));
        RawSchema prefixed = new PrefixedRawSchema(new IndexProviderRawSchema(provider), prefix);
        RawSchema filtered = new PartitionFilteredRawSchema(prefixed, () -> null);
        return new CachedRawSchema(Collections::emptyList, filtered);
    }

    @Test
    public void querySelectsOnlyBucketsThatCanMatch() {
        Assert.assertEquals(set("idx_fire_2000"), selected(P.eq(utc(1950, 6, 15, 0, 0))));
        Assert.assertEquals(set(), selected(P.eq(utc(2050, 6, 15, 0, 0))));
        Assert.assertEquals(set("idx_fire_1500", "idx_fire_2000"), selected(P.gt(utc(1200, 6, 15, 0, 0))));
        Assert.assertEquals(set("idx_fire_1000", "idx_fire_1500", "idx_fire_2000"), selected(P.gte(utc(1000, 6, 15, 0, 0))));
        Assert.assertEquals(set(), selected(P.gt(utc(2050, 6, 15, 0, 0))));
        Assert.assertEquals(set("idx_fire_500", "idx_fire_1000"), selected(P.lt(utc(900, 6, 15, 0, 0))));
        Assert.assertEquals(set("idx_fire_500", "idx_fire_1000", "idx_fire_1500", "idx_fire_2000"), selected(P.lte(utc(2050, 6, 15, 0, 0))));
        Assert.assertEquals(set("idx_fire_1500", "idx_fire_2000"),
                selected(P.gte(utc(1400, 1, 1, 0, 0)).and(P.lt(utc(1600, 1, 1, 0, 0)))));
        Assert.assertEquals(set("idx_fire_500", "idx_fire_2000"),
                selected(P.within(utc(300, 6, 15, 0, 0), utc(1950, 6, 15, 0, 0))));
    }

    private static Set<String> set(String... indices) {
        return new HashSet<>(Arrays.asList(indices));
    }

    private static Set<String> selected(P<?> predicate) {
        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProvider.Impl(
                Collections.emptyList(),
                Collections.singletonList(new GraphEdgeSchema.Impl(FIRE, fire())));
        TraversalConstraint constraint = new TraversalConstraint(__.and(__.has(T.label, FIRE), __.has("date", predicate)));
        VertexControllerContext context = new PromiseVertexControllerContext(null, new StepDescriptor(mock(Step.class)),
                schemaProvider, Optional.of(constraint), Collections.emptyList(), 0, Collections.emptyList());
        SearchBuilder searchBuilder = new SearchBuilder();
        new PromiseEdgeIndexAppender().append(searchBuilder, context);
        return new HashSet<>(searchBuilder.getIndices());
    }

    private static PartitionResolver resolver() {
        IndexPartitions partitions = fire();
        return new PartitionResolver() {
            @Override
            public IndexPartitions getPartition(String type) {
                return partitions;
            }

            @Override
            public List<IndexPartitions.Partition> getPartitions(String type) {
                return Lists.newArrayList(partitions.getPartitions());
            }
        };
    }

    private static DocumentBuilder fireDoc(String date) {
        return new DocumentBuilder(JsonNodeFactory.instance.objectNode(), "1", FIRE, Optional.empty(),
                Optional.of(new Tuple2<>("date", date)));
    }
}
