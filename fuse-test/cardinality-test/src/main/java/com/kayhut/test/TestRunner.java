package com.kayhut.test;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;

import java.util.*;

public class TestRunner {
    public static void main(String[] args) throws CardinalityMergeException {
        Random random = new Random(1234);

        GaussianFieldGenerator g = new GaussianFieldGenerator("a", 1, random, 10,20,15,3);

        int[] agg = new int[11];
        for (int i = 0; i < 100000; i++) {
            g.generateValues().forEach(j -> agg[j-10]++);
        }
        System.out.println(Arrays.toString(agg));


        List<FieldGenerator> fieldGeneratorList = new ArrayList<>();
        fieldGeneratorList.add(new UniformFieldGenerator("age", 3,random,1,120 ));
        InstanceGenerator generator = new InstanceGenerator(fieldGeneratorList);
        HLLHistogram<Integer, Integer> histogram = new HLLHistogram<>();
        int p = 6;
        int sp = 16;
        for(int i = 1;i<=120;i+=10){
            histogram.addBucket(new HLLHistogramBucket<>(i, i+10,p,sp));
        }

        for (int i = 0; i < 1000000; i++) {
            Instance instance = generator.generateInstance();


            for (Integer age : instance.getValues().get("age")) {
                Optional<HLLHistogramBucket<Integer, Integer>> bucket = histogram.findBucket(age);
                bucket.get().addBucketObject(instance.getInstanceId());


            }

        }

        Set<Integer> merge = new HashSet<>();
        HyperLogLogPlus hll = new HyperLogLogPlus(p,sp);
        for (HLLHistogramBucket<Integer, Integer> bucket : histogram.getBuckets()) {
            merge.addAll(bucket.getObjects());
            System.out.println("lower: " + bucket.getLowerBound() + ",upper: " + bucket.getUpperBound());
            System.out.println("actual card " + bucket.getObjects().size());
            System.out.println("estimation " + bucket.getHll().cardinality());
            hll = (HyperLogLogPlus) bucket.getHll().merge(hll);
        }

        System.out.println("Total objects " + merge.size());
        System.out.println("Total estimate " + hll.cardinality());
    }
}
