package com.kayhut.test.tests;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.google.common.math.Stats;
import com.kayhut.test.*;
import com.kayhut.test.generation.FieldGenerator;
import com.kayhut.test.generation.IdGenerator;
import com.kayhut.test.generation.InstanceGenerator;
import com.kayhut.test.histogram.HLLHistogram;
import com.kayhut.test.histogram.HLLHistogramBucket;
import com.kayhut.test.util.HLLUtils;
import javaslang.collection.Stream;

import java.util.*;

public class MultiModalGaussianIntersectionTest extends MultiModalGaussianIntersectionBase{

    public MultiModalGaussianIntersectionTest(Random random, int p, int sp, int numInstances, int bucketWidth, double overlapRatio) {
        super(random, overlapRatio);
        this.p = p;
        this.sp = sp;
        this.numInstances = numInstances;
        this.bucketWidth = bucketWidth;
    }

    public MultiModalGaussianIntersectionTest(Random random, int p, int sp, int numInstances, int bucketWidth, double overlapRatio, int numFields) {
        super(random, overlapRatio, numFields);
        this.p = p;
        this.sp = sp;
        this.numInstances = numInstances;
        this.bucketWidth = bucketWidth;
    }



    public TestResults run() throws CardinalityMergeException {
        List<FieldGenerator> fieldGenerators = getFieldGenerators();
        IdGenerator idGenerator = new IdGenerator();
        List<InstanceGenerator> instanceGenerators = Stream.ofAll(fieldGenerators)
                                                .map(f -> new InstanceGenerator(Collections.singletonList(f), idGenerator))
                                                .toJavaList();

        HLLHistogram<Integer, Integer> fieldValueHistogram = createFieldValueHistogram(p, sp);
        HLLHistogram<Integer, Integer> fieldHistogram = createFieldHistogram(p, sp);


        for (int i = 0; i < numInstances; i++) {

            int fieldId = random.nextInt(numFields);
            Instance instance = instanceGenerators.get(fieldId).generateInstance();
            instance.getValues().forEach((k,v) -> v.forEach(vi -> {
                Optional<HLLHistogramBucket<Integer, Integer>> bucket = fieldValueHistogram.findBucket(vi);
                bucket.get().addBucketObject(instance.getInstanceId());
            }));
            Optional<HLLHistogramBucket<Integer, Integer>> bucket = fieldHistogram.findBucket(fieldId + 1);
            bucket.get().addBucketObject(instance.getInstanceId());
        }

        Double averageBucketSize = Stream.ofAll(fieldValueHistogram.getBuckets()).filter(b -> b.getObjects().size() > 0).map(b -> b.getObjects().size()).average().get();
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
                long intersect = HLLUtils.intersect(fieldBucket.getHll(), valueBucket.getHll());
                estimatedSize.add(intersect);
                sizeDiff.add(Math.abs(intersect - valueObjects.size()));
                if(valueObjects.size() != 0){
                    diffRatio.add(((double)Math.abs(intersect - valueObjects.size())) / valueObjects.size());
                }else{
                    zeroDiff.add(Math.abs(intersect - valueObjects.size()));
                }
            }
        }

        TestResults results = new TestResults();
        results.setErrorStats(Stats.of(diffRatio));
        results.setAverageBucketSize(averageBucketSize);
        results.setAbsoluteErrorStats(Stats.of(sizeDiff));
        results.setZeroAbsoluteErrorStats(Stats.of(zeroDiff));
        results.setIntersectionSizesStats(Stats.of(actualSize));
        return results;


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

        for(int i = 1;i<=numFields;i++){
            fieldHistogram.addBucket(new HLLHistogramBucket<>(i, i+1,p,sp));
        }
        return fieldHistogram;
    }


    private int bucketWidth;
    private int p;
    private int sp;
    private int numInstances;

}
