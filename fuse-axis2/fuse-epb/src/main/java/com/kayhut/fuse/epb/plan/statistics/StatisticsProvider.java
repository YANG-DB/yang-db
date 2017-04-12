package com.kayhut.fuse.epb.plan.statistics;

/**
 * Created by moti on 31/03/2017.
 */
public interface StatisticsProvider<I> {
    CardinalityStatistics getCardinalityStatistics(I item);
    <T extends Comparable<T>> HistogramStatistics<T> getHistogramStatistics(I item);
}
