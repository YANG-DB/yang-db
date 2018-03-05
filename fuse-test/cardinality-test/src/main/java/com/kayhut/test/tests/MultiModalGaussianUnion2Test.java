package com.kayhut.test.tests;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;
import com.google.common.math.Stats;
import com.kayhut.test.*;
import com.kayhut.test.generation.FieldGenerator;
import com.kayhut.test.generation.GaussianFieldGenerator;
import com.kayhut.test.generation.IdGenerator;
import com.kayhut.test.generation.InstanceGenerator;
import com.kayhut.test.histogram.HLLHistogram;
import com.kayhut.test.histogram.HLLHistogramBucket;
import javaslang.collection.Stream;

import java.util.*;

public class MultiModalGaussianUnion2Test implements Test {



    public MultiModalGaussianUnion2Test(Random random, int p, int sp, int numInstances) {

        this.random = random;
        this.p = p;
        this.sp = sp;
        this.numInstances = numInstances;
    }

    public TestResults run() throws CardinalityMergeException {
        List<FieldGenerator> fieldGenerators = getFieldGenerators();
        IdGenerator idGenerator = new IdGenerator();
        List<InstanceGenerator> instanceGenerators = Stream.ofAll(fieldGenerators).map(f -> {
            return new InstanceGenerator(Collections.singletonList(f), idGenerator);
        }).toJavaList();

        HLLHistogram<Integer, Integer> fieldValueHistogram = createFieldValueHistogram(p, sp);
        HLLHistogram<Integer, Integer> fieldHistogram = createFieldHistogram(p, sp);


        for (int i = 0; i < numInstances; i++) {
            Instance instance = instanceGenerators.get(i%3).generateInstance();
            instance.getValues().forEach((k,v) -> {
                v.forEach(vi -> {
                    Optional<HLLHistogramBucket<Integer, Integer>> bucket = fieldValueHistogram.findBucket(vi);
                    bucket.get().addBucketObject(instance.getInstanceId());
                });

            });
            Optional<HLLHistogramBucket<Integer, Integer>> bucket = fieldHistogram.findBucket((i % 3) + 1);
            bucket.get().addBucketObject(instance.getInstanceId());
        }

        Double averageBucketSize = Stream.ofAll(fieldValueHistogram.getBuckets()).filter(b -> b.getObjects().size() > 0).map(b -> b.getObjects().size()).average().get();
        List<Long> estimatedSize = new ArrayList<>();
        List<Long> actualSize = new ArrayList<>();
        List<Double> diffRatio = new ArrayList<>();

        for (HLLHistogramBucket<Integer, Integer> valueBucket1 : fieldHistogram.getBuckets()) {
            if(valueBucket1.getObjects().size() == 0) {
                continue;
            }

            for(HLLHistogramBucket<Integer, Integer> valueBucket2 : fieldValueHistogram.getBuckets()){
                if(valueBucket2.getObjects().size() == 0 ) {
                    continue;
                }
                Set<Integer> values = new HashSet<>(valueBucket1.getObjects());
                values.addAll(valueBucket2.getObjects());

                HyperLogLogPlus merged = (HyperLogLogPlus) valueBucket1.getHll().merge(valueBucket2.getHll());
                estimatedSize.add(merged.cardinality());
                actualSize.add((long) values.size());
                diffRatio.add(((double)Math.abs(merged.cardinality() - values.size())) / values.size());
            }

        }

        TestResults results = new TestResults();
        results.setErrorStats(Stats.of(diffRatio));
        results.setAverageBucketSize(averageBucketSize);

        return results;


    }

    private List<FieldGenerator> getFieldGenerators( ){
        List<FieldGenerator> fieldGeneratorList = new ArrayList<>();
        fieldGeneratorList.add(new GaussianFieldGenerator("field1",3,random,1,100,50,30));
        fieldGeneratorList.add(new GaussianFieldGenerator("field2",3,random,80,180,150,30));
        fieldGeneratorList.add(new GaussianFieldGenerator("field3",3,random,1000,1100,1050,30));
        return fieldGeneratorList;
    }

    private HLLHistogram<Integer, Integer> createFieldValueHistogram(int p, int sp){
        HLLHistogram<Integer, Integer> fieldHistogram = new HLLHistogram<>();

        for(int i = histogramLower;i<=histogramUpper;i+=bucketWidth){
            fieldHistogram.addBucket(new HLLHistogramBucket<>(i, i+bucketWidth,p,sp));
        }
        return fieldHistogram;
    }

    private HLLHistogram<Integer, Integer> createFieldHistogram(int p, int sp){
        HLLHistogram<Integer, Integer> fieldHistogram = new HLLHistogram<>();

        for(int i = 1;i<=3;i++){
            fieldHistogram.addBucket(new HLLHistogramBucket<>(i, i+1,p,sp));
        }
        return fieldHistogram;
    }

    private int histogramLower = 1;
    private int histogramUpper = 1101;
    private int bucketWidth = 5;
    private Random random;
    private int p;
    private int sp;
    private int numInstances;

}
