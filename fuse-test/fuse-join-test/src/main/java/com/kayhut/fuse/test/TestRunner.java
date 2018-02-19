package com.kayhut.fuse.test;

import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    public static void run(TestCase testCase, TestSetupBase testSetup) throws Exception {

        testSetup.init();

        FuseManager smartEpbFuse= new FuseManager("application.engine2.dev.M2.discrete.conf", "m2.smartEpb");
        List<Long> times = new ArrayList<>();
        List<Long> queryTimes = new ArrayList<>();
        smartEpbFuse.init();
        for(int i = 0;i<10;i++) {
            testCase.run(smartEpbFuse.getFuseClient());
            if(i != 0) {
                times.add(testCase.getTotalTime());
                queryTimes.add(testCase.getPlanTime());
            }
        }
        smartEpbFuse.cleanup();

        long joinTimes =  Stream.ofAll(times).reduce((a, b) -> a+b) / times.size();
        long joinQueryTime = Stream.ofAll(queryTimes).reduce((a,b) -> a+b) / queryTimes.size();
        times.clear();
        queryTimes.clear();
        FuseManager dfsFuse = new FuseManager("application.engine2.dev.M2.discrete.conf", "m1.dfs.redundant");
        dfsFuse.init();
        for(int i = 0;i<10;i++) {

            testCase.run(dfsFuse.getFuseClient());

            if(i != 0) {
                times.add(testCase.getTotalTime());
                queryTimes.add(testCase.getPlanTime());
            }

        }
        dfsFuse.cleanup();
        System.out.println("Join average time:" + joinTimes + ", query time: " + joinQueryTime);
        System.out.println("Dfs average time:" + Stream.ofAll(times).reduce((a,b) -> a+b) / times.size()+ ", query time: " + Stream.ofAll(queryTimes).reduce((a,b) -> a+b) / queryTimes.size());
        testSetup.cleanup();
    }
}
