package com.kayhut.fuse.unipop.process.traversal.traverser;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ImmutablePath;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Roman on 1/27/2018.
 */
public class ThinPathTest {
    @Test
    public void test1() throws InterruptedException {
        long start = System.currentTimeMillis();

        ThinPath protoPath = new ThinPath(new HashStringOrdinalDictionary(), (byte)100);

        List<Path> paths = new ArrayList<>(10000);
        for(int i = 0; i < 10000 ; i++) {
            Path path = protoPath.clone();
            for(int j = 0 ; j < 100 ; j++) {
                path = path.extend(new Object(), Collections.singleton("label" + j));
            }
            paths.add(path);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("elapsed: " + elapsed);

        while(true) {
            Thread.sleep(10000);
            System.out.println(paths.size());
        }
    }

    @Test
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
        System.out.println("elapsed: " + elapsed);

        while(true) {
            Thread.sleep(10000);
            System.out.println(paths.size());
        }
    }
}
