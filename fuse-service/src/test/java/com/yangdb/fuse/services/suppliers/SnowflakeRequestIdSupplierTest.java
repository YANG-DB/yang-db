package com.yangdb.fuse.services.suppliers;

import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.model.Range;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * The worker id is the generated counter modulo the 2^6 worker ids the 6 worker bits hold (KNOWN_ISSUES A7).
 */
public class SnowflakeRequestIdSupplierTest {

    @Test
    public void workerIdUsesAllSixWorkerBits() {
        Assert.assertEquals(6, workerIdFor(70));
        Assert.assertEquals(63, workerIdFor(63));
        Assert.assertEquals(0, workerIdFor(64));
    }

    @Test
    public void requestIdsAreUnique() {
        SnowflakeRequestIdSupplier supplier = new SnowflakeRequestIdSupplier(generator(5));
        Assert.assertNotEquals(supplier.get(), supplier.get());
    }

    @Test
    public void clockSteppingBackDoesNotFailAndIdsKeepIncreasing() {
        long[] now = {SnowflakeRequestIdSupplier.EPOCH + 100_000};
        SnowflakeRequestIdSupplier supplier = new SnowflakeRequestIdSupplier(generator(5), () -> now[0]);
        long before = supplier.nextId();
        now[0] -= 52_000; // the clock steps back 52 seconds
        long during = supplier.nextId();
        long next = supplier.nextId();
        now[0] += 60_000; // and catches up
        long after = supplier.nextId();
        Assert.assertTrue(before < during);
        Assert.assertTrue(during < next);
        Assert.assertTrue(next < after);
    }

    @Test
    public void manyIdsInOneMillisecondStayUnique() {
        SnowflakeRequestIdSupplier supplier = new SnowflakeRequestIdSupplier(generator(5), () -> SnowflakeRequestIdSupplier.EPOCH + 1);
        java.util.Set<Long> ids = new java.util.HashSet<>();
        long last = -1;
        for (int i = 0; i < 200_000; i++) { // more than the 65,536 a millisecond holds
            long id = supplier.nextId();
            Assert.assertTrue(id > last);
            last = id;
            ids.add(id);
        }
        Assert.assertEquals(200_000, ids.size());
    }

    private static long workerIdFor(long counter) {
        SnowflakeRequestIdSupplier supplier = new SnowflakeRequestIdSupplier(generator(counter));
        Assert.assertTrue(supplier.get().startsWith("FR"));
        return supplier.getWorkerId();
    }

    private static IdGeneratorDriver<Range> generator(long lower) {
        return new IdGeneratorDriver<Range>() {
            @Override
            public Range getNext(String genName, int numIds) {
                return new Range(lower, lower + numIds);
            }

            @Override
            public boolean init(List<String> names) {
                return true;
            }
        };
    }
}
