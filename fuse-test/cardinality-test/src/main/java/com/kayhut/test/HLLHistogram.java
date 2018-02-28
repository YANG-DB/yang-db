package com.kayhut.test;

import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HLLHistogram<T extends Comparable<T>, TObject> {
    public HLLHistogram() {
        this.buckets = new ArrayList<>();
    }

    public void addBucket(HLLHistogramBucket<T, TObject> bucket){
        for (int i = 0; i < buckets.size(); i++) {
            if(buckets.get(i).getLowerBound().compareTo(bucket.getUpperBound()) == 0){
                buckets.add(i, bucket);
                return;
            }
        }
        buckets.add(bucket);
    }

    public List<HLLHistogramBucket<T, TObject>> getBuckets() {
        return buckets;
    }

    public Optional<HLLHistogramBucket<T, TObject>> findBucket(T value){
        return Stream.ofAll(this.buckets).find(b -> b.getLowerBound().compareTo(value)<=0 && b.getUpperBound().compareTo(value) >=0).toJavaOptional();
    }

    private List<HLLHistogramBucket<T, TObject>> buckets;
}
