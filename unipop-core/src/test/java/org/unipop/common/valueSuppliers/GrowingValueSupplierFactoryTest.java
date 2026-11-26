package org.unipop.common.valueSuppliers;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class GrowingValueSupplierFactoryTest {

    @Test
    public void growsByTheMultiplierUpToTheMax() {
        Supplier<Supplier<Integer>> sizes = new GrowingValueSupplierFactory(1000, 2, 10000);
        List<Integer> seen = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Supplier<Integer> bulk = sizes.get();
            Assert.assertEquals(bulk.get(), bulk.get()); // fixed within a bulk
            seen.add(bulk.get());
        }
        Assert.assertEquals(Arrays.asList(1000, 2000, 4000, 8000, 10000, 10000, 10000), seen);
    }

    @Test
    public void multiplierOneIsAFixedSize() {
        Supplier<Supplier<Integer>> sizes = new GrowingValueSupplierFactory(500, 1, 10000);
        Assert.assertEquals(500, (int) sizes.get().get());
        Assert.assertEquals(500, (int) sizes.get().get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void startAboveMaxIsRejected() {
        new GrowingValueSupplierFactory(2000, 2, 1000);
    }
}
