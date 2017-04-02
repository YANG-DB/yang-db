package com.kayhut.fuse.epb.plan.statistics;

/**
 * Created by moti on 31/03/2017.
 */
public interface StatisticsProvider<S, I> {
    S getStatistics(I item);
}
