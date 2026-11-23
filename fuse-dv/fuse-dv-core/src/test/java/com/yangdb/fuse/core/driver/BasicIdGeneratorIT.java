package com.yangdb.fuse.core.driver;

import com.yangdb.fuse.model.Range;
import com.yangdb.test.BaseITMarker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.yangdb.fuse.executor.TestSuiteIndexProviderSuite.client;

/**
 * The id generator (KNOWN_ISSUES A6): consecutive calls hand out consecutive, non-overlapping ranges, re-running
 * {@code init} doesn't reset a counter, and generators that don't share a lock (e.g. two service nodes) never get the
 * same range.
 */
public class BasicIdGeneratorIT implements BaseITMarker {
    private static final String INDEX = "idgen_it";

    @Before
    public void setUp() {
        dropIndex();
    }

    @After
    public void tearDown() {
        dropIndex();
    }

    @Test
    public void consecutiveCallsGetConsecutiveRanges() {
        BasicIdGenerator generator = new BasicIdGenerator(client, INDEX);
        Range first = generator.getNext("demo", 10);
        Range second = generator.getNext("demo", 10);
        Range other = generator.getNext("other", 5);

        Assert.assertEquals(1, first.getLower());
        Assert.assertEquals(11, first.getUpper());
        Assert.assertEquals(11, second.getLower());
        Assert.assertEquals(21, second.getUpper());
        Assert.assertEquals("counters are per name", 1, other.getLower());
    }

    @Test
    public void initDoesNotResetACounter() {
        BasicIdGenerator generator = new BasicIdGenerator(client, INDEX);
        Assert.assertTrue(generator.init(Collections.singletonList("demo")));
        Assert.assertEquals(1, generator.getNext("demo", 10).getLower());
        Assert.assertTrue(generator.init(Collections.singletonList("demo")));
        Assert.assertEquals(11, generator.getNext("demo", 10).getLower());
    }

    @Test
    public void concurrentGeneratorsNeverShareARange() throws Exception {
        new BasicIdGenerator(client, INDEX).init(Collections.singletonList("demo"));
        int generators = 4, callsEach = 10, numIds = 3;
        ExecutorService pool = Executors.newFixedThreadPool(generators);
        try {
            List<Future<List<Range>>> futures = new ArrayList<>();
            for (int g = 0; g < generators; g++) {
                //one generator per thread: no shared in-process lock, like separate service nodes
                BasicIdGenerator generator = new BasicIdGenerator(client, INDEX);
                Callable<List<Range>> calls = () -> {
                    List<Range> ranges = new ArrayList<>();
                    for (int i = 0; i < callsEach; i++) {
                        ranges.add(generator.getNext("demo", numIds));
                    }
                    return ranges;
                };
                futures.add(pool.submit(calls));
            }
            List<Range> all = new ArrayList<>();
            for (Future<List<Range>> future : futures) {
                all.addAll(future.get());
            }
            all.sort(Comparator.comparingLong(Range::getLower));

            long expectedLower = 1;
            for (Range range : all) {
                Assert.assertEquals("ranges are contiguous and disjoint: " + all, expectedLower, range.getLower());
                Assert.assertEquals(expectedLower + numIds, range.getUpper());
                expectedLower = range.getUpper();
            }
            Assert.assertEquals(generators * callsEach, all.size());
        } finally {
            pool.shutdownNow();
        }
    }

    private static void dropIndex() {
        if (client.admin().indices().exists(new IndicesExistsRequest(INDEX)).actionGet().isExists()) {
            client.admin().indices().delete(new DeleteIndexRequest(INDEX)).actionGet();
        }
    }
}
