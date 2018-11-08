package org.unipop.common.valueSuppliers;

import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * Created by Roman on 8/21/2018.
 */
public class LinearBufferedSupplierFactoryTests {
    @Test
    @Ignore
    public void test1() throws InterruptedException {
        long[] timeBuffer = new long[] {1000L, 2000L, 3000L, 4000L, 5000L, 6000L, 7000L, 8000L, 9000L, 10000L, 11000L};
        int[] bulkSizeBuffer = new int[] {1000, 900, 800, 700, 600, 500, 400, 300, 200, 100, 1};

        Supplier<Integer> bulkSizeSupplier = new LinearBufferedSupplierFactory(timeBuffer, bulkSizeBuffer).get();
        for(int i = 0 ; i < 20 ; i++) {
            System.out.println(bulkSizeSupplier.get());
            Thread.sleep(1000);
        }
    }

    @Test
    @Ignore
    public void test2() throws InterruptedException {
        long[] timeBuffer = new long[] {100L, 200L, 300L, 400L, 500L, 600L, 700L, 800L, 900L, 1000L, 1100L};
        int[] bulkSizeBuffer = new int[] {1000, 900, 800, 700, 600, 500, 400, 300, 200, 100, 1};

        Supplier<Integer> bulkSizeSupplier = new LinearBufferedSupplierFactory(timeBuffer, bulkSizeBuffer).get();
        for(int i = 0 ; i < 20 ; i++) {
            System.out.println(bulkSizeSupplier.get());
            Thread.sleep(100);
        }
    }
}
