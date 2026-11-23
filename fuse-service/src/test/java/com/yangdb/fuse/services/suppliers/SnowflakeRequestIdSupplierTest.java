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
