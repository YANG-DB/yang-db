package com.yangdb.fuse.unipop.process.traversal.traverser;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ImmutablePath;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

/**
 * Created by Roman on 1/27/2018.
 */
public class ThinPathTest {
    @Test
    @Ignore
    public void test1() throws InterruptedException {
        long start = System.currentTimeMillis();

        ThinPath protoPath = new ThinPath(new HashStringOrdinalDictionary());

        List<Path> paths = new ArrayList<>(10000);
        for(int i = 0; i < 10000 ; i++) {
            Path path = protoPath.clone();
            for(int j = 0 ; j < 100 ; j++) {
                path = path.extend(new Object(), Collections.singleton("label" + j));
            }
            paths.add(path);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("create paths elapsed: " + elapsed);

        start = System.currentTimeMillis();
        for(int i = 0 ; i < 10000 ; i++) {
            for(int j = 0 ; j < 100 ; j++) {
                paths.get(i).get("label" + j);
            }
        }
        elapsed = System.currentTimeMillis() - start;
        System.out.println("read labels elapsed: " + elapsed);

        while(true) {
            Thread.sleep(10000);
            System.out.println(paths.size());
        }
    }

    @Test
    @Ignore
    public void test2() throws InterruptedException {
        long start = System.currentTimeMillis();

        List<Path> paths = new ArrayList<>(10000);
        for(int i = 0; i < 10000 ; i++) {
            Path path = ImmutablePath.make();
            for(int j = 0 ; j < 100 ; j++) {
                path = path.extend(new Object(), Collections.singleton("label" + j));
            }
            paths.add(path);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("create paths elapsed: " + elapsed);

        start = System.currentTimeMillis();
        for(int i = 0 ; i < 10000 ; i++) {
            for(int j = 0 ; j < 100 ; j++) {
                paths.get(i).get("label" + j);
            }
        }
        elapsed = System.currentTimeMillis() - start;
        System.out.println("read labels elapsed: " + elapsed);

        while(true) {
            Thread.sleep(10000);
            System.out.println(paths.size());
        }
    }
}
