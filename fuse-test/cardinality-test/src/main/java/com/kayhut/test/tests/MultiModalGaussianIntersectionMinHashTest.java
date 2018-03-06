package com.kayhut.test.tests;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.ICardinality;
import com.google.common.math.Stats;
import com.kayhut.test.Instance;
import com.kayhut.test.generation.FieldGenerator;
import com.kayhut.test.generation.IdGenerator;
import com.kayhut.test.generation.InstanceGenerator;
import com.kayhut.test.histogram.HLLMinHashHistogram;
import com.kayhut.test.histogram.HLLMinHashHistogramBucket;
import javaslang.collection.Stream;

import java.util.*;

public class MultiModalGaussianIntersectionMinHashTest extends MultiModalGaussianIntersectionBase{

    public MultiModalGaussianIntersectionMinHashTest(Random random, int p, int sp, int numInstances, int bucketWidth, int k, double overlapRatio) {
        super(random, overlapRatio);
        this.p = p;
        this.sp = sp;
        this.numInstances = numInstances;
        this.bucketWidth = bucketWidth;
        this.k = k;
    }

    public MultiModalGaussianIntersectionMinHashTest(Random random, int p, int sp, int numInstances, int bucketWidth, int k, double overlapRatio, int numFields) {
        super(random, overlapRatio,numFields);
        this.p = p;
        this.sp = sp;
        this.numInstances = numInstances;
        this.bucketWidth = bucketWidth;
        this.k = k;
    }

    public TestResults run() throws CardinalityMergeException {
        List<FieldGenerator> fieldGenerators = getFieldGenerators();
        IdGenerator idGenerator = new IdGenerator();
        List<InstanceGenerator> instanceGenerators = Stream.ofAll(fieldGenerators).map(f -> {
            return new InstanceGenerator(Collections.singletonList(f), idGenerator);
        }).toJavaList();

        HLLMinHashHistogram<Integer, Integer> fieldValueHistogram = createFieldValueHistogram(p, sp);
        HLLMinHashHistogram<Integer, Integer> fieldHistogram = createFieldHistogram(p, sp);


        for (int i = 0; i < numInstances; i++) {
            int fieldId = random.nextInt(numFields);
            Instance instance = instanceGenerators.get(fieldId).generateInstance();
            instance.getValues().forEach((k,v) -> {
                v.forEach(vi -> {
                    Optional<HLLMinHashHistogramBucket<Integer, Integer>> bucket = fieldValueHistogram.findBucket(vi);
                    bucket.get().addBucketObject(instance.getInstanceId());
                });

            });
            Optional<HLLMinHashHistogramBucket<Integer, Integer>> bucket = fieldHistogram.findBucket(fieldId + 1);
            bucket.get().addBucketObject(instance.getInstanceId());
        }

        Double averageBucketSize = Stream.ofAll(fieldValueHistogram.getBuckets()).filter(b -> b.getObjects().size() > 0).map(b -> b.getObjects().size()).average().get();
        List<Long> estimatedSize = new ArrayList<>();
        List<Long> actualSize = new ArrayList<>();
        List<Long> bucketSize = new ArrayList<>();
        List<Long> sizeDiff = new ArrayList<>();
        List<Double> diffRatio = new ArrayList<>();
        List<Long> zeroDiff = new ArrayList<>();

        for (HLLMinHashHistogramBucket<Integer, Integer> fieldBucket : fieldHistogram.getBuckets()) {
            for (HLLMinHashHistogramBucket<Integer, Integer> valueBucket : fieldValueHistogram.getBuckets()) {
                if(valueBucket.getObjects().size() == 0) {
                    continue;
                }
                HashSet<Integer> valueObjects = new HashSet<>(valueBucket.getObjects());
                valueObjects.retainAll(fieldBucket.getObjects());

                bucketSize.add((long) valueBucket.getObjects().size());
                actualSize.add((long) valueObjects.size());
                ICardinality merge = fieldBucket.getHll().merge(valueBucket.getHll());
                long intersect = (long) Math.ceil(merge.cardinality() * fieldBucket.getMinHash().estimateJaccard(valueBucket.getMinHash()));
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

    private HLLMinHashHistogram<Integer, Integer> createFieldValueHistogram(int p, int sp){
        HLLMinHashHistogram<Integer, Integer> fieldHistogram = new HLLMinHashHistogram<>();

        for(int i = histogramLower;i<=histogramUpper;i+=bucketWidth){
            fieldHistogram.addBucket(new HLLMinHashHistogramBucket<>(i, i+bucketWidth,p,sp,k));
        }
        return fieldHistogram;
    }

    private HLLMinHashHistogram<Integer, Integer> createFieldHistogram(int p, int sp){
        HLLMinHashHistogram<Integer, Integer> fieldHistogram = new HLLMinHashHistogram<>();

        for(int i = 1;i<=numFields;i++){
            fieldHistogram.addBucket(new HLLMinHashHistogramBucket<>(i, i+1,p,sp,k));
        }
        return fieldHistogram;
    }

    private int bucketWidth;
    private int p;
    private int sp;
    private int numInstances;
    private int k;
}
