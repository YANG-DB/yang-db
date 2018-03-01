package com.kayhut.test.tests;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
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

public class MultiModalGaussianIntersectionExactTest implements Test{

    public MultiModalGaussianIntersectionExactTest(Random random, int p, int sp, int numInstances) {

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

        System.out.println(Stream.ofAll(fieldValueHistogram.getBuckets()).filter(b -> b.getObjects().size() > 0).map(b -> b.getObjects().size()).average().get());
        List<Long> estimatedSize = new ArrayList<>();
        List<Long> actualSize = new ArrayList<>();
        List<Long> bucketSize = new ArrayList<>();
        List<Long> sizeDiff = new ArrayList<>();
        List<Double> diffRatio = new ArrayList<>();
        List<Long> zeroDiff = new ArrayList<>();

        for (HLLHistogramBucket<Integer, Integer> fieldBucket : fieldHistogram.getBuckets()) {
            for (HLLHistogramBucket<Integer, Integer> valueBucket : fieldValueHistogram.getBuckets()) {
                if(valueBucket.getObjects().size() == 0) {
                    continue;
                }
                HashSet<Integer> valueObjects = new HashSet<>(valueBucket.getObjects());
                valueObjects.retainAll(fieldBucket.getObjects());

                bucketSize.add((long) valueBucket.getObjects().size());
                actualSize.add((long) valueObjects.size());
                long intersect = fieldBucket.getObjects().size() + valueBucket.getObjects().size() - fieldBucket.getHll().merge(valueBucket.getHll()).cardinality();

                estimatedSize.add(intersect);
                sizeDiff.add(Math.abs(intersect - valueObjects.size()));
                if(valueObjects.size() != 0){
                    diffRatio.add(((double)Math.abs(intersect - valueObjects.size())) / valueObjects.size());
                }else{
                    zeroDiff.add(Math.abs(intersect - valueObjects.size()));
                }
            }
        }

        System.out.println("Stats for absolute diff: " + Stats.of(sizeDiff));
        System.out.println("Stats for diff ratio: " + Stats.of(diffRatio));
        System.out.println("Stats for zero absolute diff: " + Stats.of(zeroDiff));
        TestResults results = new TestResults();
        results.setErrorRatios(diffRatio);
        return results;


    }

    private List<FieldGenerator> getFieldGenerators( ){
        List<FieldGenerator> fieldGeneratorList = new ArrayList<>();
        fieldGeneratorList.add(new GaussianFieldGenerator("field1",1,random,1,100,50,30));
        fieldGeneratorList.add(new GaussianFieldGenerator("field2",1,random,80,180,150,30));
        fieldGeneratorList.add(new GaussianFieldGenerator("field3",1,random,1000,1100,1050,30));
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
    private int histogramUpper=1101;
    private int bucketWidth = 10;
    private Random random;
    private int p;
    private int sp;
    private int numInstances;

}
