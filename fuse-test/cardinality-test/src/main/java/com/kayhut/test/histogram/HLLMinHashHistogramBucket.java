package com.kayhut.test.histogram;

import com.clearspring.analytics.hash.MurmurHash;
import com.clearspring.analytics.stream.cardinality.HyperLogLogPlus;

import java.util.HashSet;
import java.util.Set;

public class HLLMinHashHistogramBucket<T, TObject> {

    public HLLMinHashHistogramBucket(T lowerBound, T upperBound, int p, int sp, int k) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.hll = new HyperLogLogPlus(p,sp);
        this.objects = new HashSet<>();
        this.minHash = new MinHash(k);
    }

    public void addBucketObject(TObject obj){
        this.hll.offer(obj);
        this.objects.add(obj);
        long l = MurmurHash.hash64(obj);
        minHash.addValue(l);
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

    public MinHash getMinHash() {
        return minHash;
    }

    private T lowerBound;
    private T upperBound;
    private Set<TObject> objects;
    private HyperLogLogPlus hll;
    private MinHash minHash;
}
