package com.kayhut.fuse.epb.plan.statistics;

/**
 * Created by moti on 13/04/2017.
 */
public class RawGraphStatisticsProvider implements StatisticsProvider<RawGraphStatisticableItemInfo> {
    @Override
    public CardinalityStatistics getCardinalityStatistics(RawGraphStatisticableItemInfo item) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> HistogramStatistics<T> getHistogramStatistics(RawGraphStatisticableItemInfo item) {
        return null;
    }
}
