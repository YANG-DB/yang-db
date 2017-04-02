package com.kayhut.fuse.epb.plan.statistics;

/**
 * Created by moti on 31/03/2017.
 */
public class BucketInfo<T extends Comparable<T>> {

    public BucketInfo(Long total, Long cardinality, T lowerBound, T higherBound) {
        _total = total;
        _cardinality = cardinality;
        _lowerBound = lowerBound;
        _higherBound = higherBound;
    }

    public Long getCardinality() {
        return _cardinality;
    }

    public Long getTotal() {
        return _total;
    }

    public T getLowerBound() { return _lowerBound; };
    public T getHigherBound() { return _higherBound; };

    //lower bound - inclusive, higher bound - non-inclusive
    public boolean isValueInRange(T value) {
        if (_higherBound != null && value.compareTo(_higherBound) >= 0) {
            return false;
        }
        if (_lowerBound != null && value.compareTo(_lowerBound) < 0) {
            return false;
        }
        return true;
    }

    private T _lowerBound;
    private T _higherBound;
    private Long _total;
    private Long _cardinality;

}

