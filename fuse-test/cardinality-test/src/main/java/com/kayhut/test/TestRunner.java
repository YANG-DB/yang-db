package com.kayhut.test;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.google.common.math.Stats;
import com.kayhut.test.tests.MultiModalGaussianUnion2Test;
import com.kayhut.test.tests.TestResults;
import javaslang.Tuple2;

import java.util.*;

public class TestRunner {
    public static void main(String[] args) throws CardinalityMergeException {
        List<Tuple2<Integer, Double>> errors = new ArrayList<>();
        for (int i = 4; i < 16; i++) {
            Random random = new Random(1234);
            int p = i;
            int sp = 18;
            int numInstances = 1000000;
            MultiModalGaussianUnion2Test test = new MultiModalGaussianUnion2Test(random,p,sp,numInstances );
            System.out.println("Results for p = " + p + " ,sp = " + sp);
            TestResults results = test.run();
            errors.add(new Tuple2<>(i, Stats.meanOf(results.getErrorRatios())));

        }

        System.out.println("p\terror ratio");
        System.out.println("----------------------");
        for (int i = 0; i < errors.size(); i++) {
            System.out.println(errors.get(i)._1 + "\t" + errors.get(i)._2);
        }


    }
}
