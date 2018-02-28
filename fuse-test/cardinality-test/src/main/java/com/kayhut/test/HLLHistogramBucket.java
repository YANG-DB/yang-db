package com.kayhut.test;

import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;

import java.util.HashSet;
import java.util.Set;

public class HLLHistogramBucket<T, TObject> {

    public HLLHistogramBucket(T lowerBound, T upperBound, int p, int sp) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.hll = new HyperLogLogPlus(p,sp);
        this.objects = new HashSet<>();
    }

    public void addBucketObject(TObject obj){
        this.hll.offer(obj);
        this.objects.add(obj);
    }

    public T getLowerBound() {
        return lowerBound;
    }

    public T getUpperBound() {
        return upperBound;
    }

    public HyperLogLogPlus getHll() {
        return hll;
    }

    public Set<TObject> getObjects() {
        return objects;
    }

    private T lowerBound;
    private T upperBound;
    private Set<TObject> objects;
    private HyperLogLogPlus hll;
}
