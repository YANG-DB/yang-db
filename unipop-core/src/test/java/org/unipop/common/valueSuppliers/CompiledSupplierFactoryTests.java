package org.unipop.common.valueSuppliers;

import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * Created by Roman on 8/22/2018.
 */
public class CompiledSupplierFactoryTests {
    @Test
    @Ignore
    public void test1() throws InterruptedException {
        Supplier<Integer> bulkSizeSupplier = new CompiledSupplierFactory(
                new LinearDecayingValueSupplierFactory(1000, 1, 10000),
                10000, 100, CompiledSupplierFactory.ValueAggMethod.max).get();

        for(int i = 0 ; i < 20 ; i++) {
            System.out.println(bulkSizeSupplier.get());
            Thread.sleep(1000);
        }
    }

    @Test
    @Ignore
    public void test2() throws InterruptedException {
        Supplier<Integer> bulkSizeSupplier = new CompiledSupplierFactory(
                new LinearDecayingValueSupplierFactory(1000, 1, 10000),
                10000, 100, CompiledSupplierFactory.ValueAggMethod.min).get();

        for(int i = 0 ; i < 20 ; i++) {
            System.out.println(bulkSizeSupplier.get());
            Thread.sleep(1000);
        }
    }

    @Test
    @Ignore
    public void test3() throws InterruptedException {
        Supplier<Integer> bulkSizeSupplier = new CompiledSupplierFactory(
                new LinearDecayingValueSupplierFactory(1000, 1, 10000),
                10000, 100, CompiledSupplierFactory.ValueAggMethod.avg).get();

        for(int i = 0 ; i < 20 ; i++) {
            System.out.println(bulkSizeSupplier.get());
            Thread.sleep(1000);
        }
    }

    @Test
    @Ignore
    public void test4() throws InterruptedException {
        Supplier<Integer> bulkSizeSupplier = new CompiledSupplierFactory(
                new LinearDecayingValueSupplierFactory(1000, 1, 1000),
                1000, 100, CompiledSupplierFactory.ValueAggMethod.max).get();

        for(int i = 0 ; i < 20 ; i++) {
            System.out.println(bulkSizeSupplier.get());
            Thread.sleep(100);
        }
    }

    @Test
    @Ignore
    public void test5() throws InterruptedException {
        Supplier<Integer> bulkSizeSupplier = new CompiledSupplierFactory(
                new LinearDecayingValueSupplierFactory(1000, 1, 1000),
                1000, 60, CompiledSupplierFactory.ValueAggMethod.max).get();

        for(int i = 0 ; i < 50 ; i++) {
            System.out.println(bulkSizeSupplier.get());
            Thread.sleep(30);
        }
    }
}
