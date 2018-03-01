package com.kayhut.test.tests;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;
import com.google.common.math.Stats;
import com.kayhut.test.*;
import com.kayhut.test.generation.FieldGenerator;
import com.kayhut.test.generation.IdGenerator;
import com.kayhut.test.generation.InstanceGenerator;
import com.kayhut.test.generation.UniformFieldGenerator;
import com.kayhut.test.histogram.HLLHistogram;
import com.kayhut.test.histogram.HLLHistogramBucket;
import javaslang.collection.Stream;

import java.util.*;

public class SimpleUniformTest implements Test{

    public SimpleUniformTest(int fieldLower, int fieldUpper, int bucketWidth, Random random, int maxNumValues, String fieldName, int p, int sp, int numInstances) {
        this.fieldLower = fieldLower;
        this.fieldUpper = fieldUpper;
        this.bucketWidth = bucketWidth;
        this.random = random;
        this.maxNumValues = maxNumValues;
        this.fieldName = fieldName;
        this.p = p;
        this.sp = sp;
        this.numInstances = numInstances;
    }

    public TestResults run() throws CardinalityMergeException {
        InstanceGenerator generator = new InstanceGenerator(getFieldGenerators(),new IdGenerator());

        Map<String, HLLHistogram<Integer, Integer>> histograms = createHistograms(p, sp);
        HLLHistogram<Integer, Integer> histogram = histograms.get(fieldName);
        for (int i = 0; i < numInstances; i++) {
            Instance instance = generator.generateInstance();
            for (Integer value : instance.getValues().get(fieldName)) {
                Optional<HLLHistogramBucket<Integer, Integer>> bucket = histogram.findBucket(value);
                bucket.get().addBucketObject(instance.getInstanceId());
            }
        }

        Set<Integer> merge = new HashSet<>();
        HyperLogLogPlus hll = new HyperLogLogPlus(p,sp);


        List<Long> diffs = new ArrayList<>();
        for (HLLHistogramBucket<Integer, Integer> bucket : histogram.getBuckets()) {
            merge.addAll(bucket.getObjects());
            diffs.add(Math.abs(bucket.getObjects().size() - bucket.getHll().cardinality()));
            hll = (HyperLogLogPlus) bucket.getHll().merge(hll);
        }
        Stats diffsStats = Stats.of(diffs);
        System.out.println(Stats.of(Stream.ofAll(histogram.getBuckets()).map(b -> b.getObjects().size())));
        System.out.println(diffsStats);

        return new TestResults();
    }

    private List<FieldGenerator> getFieldGenerators( ){
        List<FieldGenerator> fieldGeneratorList = new ArrayList<>();
        fieldGeneratorList.add(new UniformFieldGenerator(fieldName, maxNumValues,random,fieldLower,fieldUpper ));
        return fieldGeneratorList;
    }

    private Map<String ,HLLHistogram<Integer, Integer>> createHistograms(int p, int sp){
        HLLHistogram<Integer, Integer> fieldHistogram = new HLLHistogram<>();

        for(int i = fieldLower;i<=fieldUpper;i+=bucketWidth){
            fieldHistogram.addBucket(new HLLHistogramBucket<>(i, i+bucketWidth,p,sp));
        }
        Map<String ,HLLHistogram<Integer, Integer>> histogramMap = new HashMap<>();
        histogramMap.put(fieldName, fieldHistogram);
        return histogramMap;
    }

    private int fieldLower;
    private int fieldUpper;
    private int bucketWidth;
    private Random random;
    private int maxNumValues;
    private String fieldName;
    private int p;
    private int sp;
    private int numInstances;
}
