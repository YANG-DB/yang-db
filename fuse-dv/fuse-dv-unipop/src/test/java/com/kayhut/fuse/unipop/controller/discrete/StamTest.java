package com.kayhut.fuse.unipop.controller.discrete;

import javaslang.collection.Stream;
import org.jooq.lambda.Seq;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StamTest {
    @Test
    @Ignore
    public void test1() {
        Iterator<Integer> iterator = Stream.ofAll(() -> new MyIterator())
                .flatMap(bulk -> Stream.ofAll(() -> process(bulk)))
                .iterator();

        while(iterator.hasNext()) {
            int a = iterator.next();
            int x = 5;
        }
    }

    @Test
    @Ignore
    public void test2() {
        Iterator<Integer> iterator = Seq.seq(new MyIterator())
                .flatMap(bulk -> Seq.seq(process(bulk)))
                .iterator();

        while(iterator.hasNext()) {
            int a = iterator.next();
            int x = 5;
        }
    }

    @Test
    @Ignore
    public void test3() {
        List<Integer> a = Arrays.asList(1, 2, 3);
        Set<Integer> b = new HashSet<>();

        long start = System.currentTimeMillis();
        for(int i = 0 ; i < 1000000 ; i++) {
            Stream<Integer> stream = Stream.ofAll(a);
            stream.forEach(b::add);
        }
        long elapsed = System.currentTimeMillis() - start;

        b.clear();
        start = System.currentTimeMillis();
        for(int i = 0 ; i < 1000000 ; i++) {
            Seq<Integer> seq = Seq.seq(a);
            seq.forEach(b::add);
        }
        elapsed = System.currentTimeMillis() - start;

        for(int j = 0 ; j < 10 ; j++) {
            b.clear();
            start = System.currentTimeMillis();
            for (int i = 0; i < 10000000; i++) {
                Stream<Integer> stream = Stream.ofAll(a);
                stream.forEach(b::add);
            }
            long elapsedJavaslang = System.currentTimeMillis() - start;

            b.clear();
            start = System.currentTimeMillis();
            for (int i = 0; i < 10000000; i++) {
                Seq<Integer> seq = Seq.seq(a);
                seq.forEach(b::add);
            }
            long elapsedSeq = System.currentTimeMillis() - start;

            System.out.println("elapsedJavaslang: " + elapsedJavaslang + ",   elapsedSeq: " + elapsedSeq);
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
