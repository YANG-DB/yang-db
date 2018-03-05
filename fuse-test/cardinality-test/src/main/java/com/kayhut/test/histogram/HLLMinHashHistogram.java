package com.kayhut.test.histogram;

import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HLLMinHashHistogram<T extends Comparable<T>, TObject> {
    public HLLMinHashHistogram() {
        this.buckets = new ArrayList<>();
    }

    public void addBucket(HLLMinHashHistogramBucket<T, TObject> bucket){
        for (int i = 0; i < buckets.size(); i++) {
            if(buckets.get(i).getLowerBound().compareTo(bucket.getUpperBound()) == 0){
                buckets.add(i, bucket);
                return;
            }
        }
        buckets.add(bucket);
    }

    public List<HLLMinHashHistogramBucket<T, TObject>> getBuckets() {
        return buckets;
    }

    public Optional<HLLMinHashHistogramBucket<T, TObject>> findBucket(T value){
        return Stream.ofAll(this.buckets).find(b -> b.getLowerBound().compareTo(value)<=0 && b.getUpperBound().compareTo(value) >0).toJavaOptional();
    }

    private List<HLLMinHashHistogramBucket<T, TObject>> buckets;
}
