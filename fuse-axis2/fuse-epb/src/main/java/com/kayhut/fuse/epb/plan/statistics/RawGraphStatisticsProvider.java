package com.kayhut.fuse.epb.plan.statistics;

import javax.inject.Singleton;

/**
 * Created by moti on 13/04/2017.
 */
public class RawGraphStatisticsProvider implements StatisticsProvider<RawGraphStatisticableItemInfo> {
    @Singleton
    public RawGraphStatisticsProvider() {
    }

    @Override
    public Statistics getStatistics(RawGraphStatisticableItemInfo item) {
        return null;
    }

    private Statistics.CardinalityStatistics getCardinality(RawGraphStatisticableItemInfo item){
        return null;
    }
}
