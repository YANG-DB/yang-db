package com.kayhut.test.histogram;

import com.clearspring.analytics.hash.MurmurHash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MinHash {

    public MinHash(int k) {
        this.k = k;
        this.values = new ArrayList<>(k);
    }

    public void addValue(long value){
        int idx = Collections.binarySearch(values, value);
        if(idx < 0){
            idx = idx*-1 - 1;
            if(idx < k){
                values.add(idx, value);
                if(values.size() > k){
                    values.remove(k);
                }
            }
        }
    }

    public MinHash merge(MinHash other){
        MinHash newMinHash = new MinHash(k);
        this.values.forEach(v -> newMinHash.addValue(v));
        other.values.forEach(v -> newMinHash.addValue(v));
        return newMinHash;
    }

    public double estimateJaccard(MinHash other){
        MinHash merged = this.merge(other);
        long y = merged.values.stream().filter(v -> this.values.contains(v) && other.values.contains(v)).count();
        return (double)y / (double)k;
    }

    private List<Long> values;
    private int k;
}
