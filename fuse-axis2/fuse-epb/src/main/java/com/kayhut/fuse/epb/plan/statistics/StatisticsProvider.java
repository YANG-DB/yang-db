package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.execution.plan.Direction;

/**
 * Created by moti on 31/03/2017.
 */
public interface StatisticsProvider<I> {
    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getStatistics(I item);
    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getRedundantStatistics(I item);
    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getRedundantStatistics(I item, I rel, Direction direction);

}
