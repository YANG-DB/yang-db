package com.kayhut.fuse.epb.plan.statistics;

import java.util.List;

/**
 * Created by moti on 31/03/2017.
 */
public class HistogramStatistics<T extends Comparable<T>> implements Statistics{
    private List<BucketInfo<T>> buckets;

    public HistogramStatistics(List<BucketInfo<T>> buckets) {
        this.buckets = buckets;
    }

    public List<BucketInfo<T>> getBuckets() {
        return buckets;
    }
}
