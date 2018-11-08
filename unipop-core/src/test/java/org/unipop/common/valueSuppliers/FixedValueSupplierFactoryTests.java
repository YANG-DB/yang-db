package org.unipop.common.valueSuppliers;

import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * Created by Roman on 8/21/2018.
 */
public class FixedValueSupplierFactoryTests {
    @Test
    @Ignore
    public void test1() throws InterruptedException {
        Supplier<Integer> bulkSizeSupplier = new FixedValueSupplierFactory(1000).get();
        for(int i = 0 ; i < 20 ; i++) {
            System.out.println(bulkSizeSupplier.get());
            Thread.sleep(1000);
        }
    }

    @Test
    @Ignore
    public void test2() throws InterruptedException {
        Supplier<Integer> bulkSizeSupplier = new FixedValueSupplierFactory(1000).get();
        for(int i = 0 ; i < 20 ; i++) {
            System.out.println(bulkSizeSupplier.get());
            Thread.sleep(100);
        }
    }
}
