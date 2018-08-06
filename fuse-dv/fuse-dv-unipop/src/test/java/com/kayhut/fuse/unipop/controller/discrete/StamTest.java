package com.kayhut.fuse.unipop.controller.discrete;

import javaslang.collection.Stream;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StamTest {
    @Test
    public void test1() {
        Iterator<Integer> iterator = Stream.ofAll(() -> new MyIterator())
                .flatMap(bulk -> Stream.ofAll(() -> process(bulk)))
                .iterator();

        while(iterator.hasNext()) {
            int a = iterator.next();
            int x = 5;
        }
    }

    private Iterator<Integer> process(List<Integer> bulk) {
        System.out.println("process bulk");
        return bulk.iterator();
    }

    public static class MyIterator implements Iterator<List<Integer>> {
        public MyIterator() {
            bulks = new ArrayList<>();
            bulks.add(Arrays.asList(1, 2, 3, 4, 5));
            bulks.add(Arrays.asList(6, 7, 8, 9, 10));
            index = 0;
        }

        @Override
        public boolean hasNext() {
            System.out.println("MyIterator - hasNext");
            return index != bulks.size();
        }

        @Override
        public List<Integer> next() {
            System.out.println("MyIterator - next");

            return bulks.get(index++);
        }

        private List<List<Integer>> bulks;
        private int index;
    }
}
