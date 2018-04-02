package com.kayhut.fuse.epb.plan.statistics;

/**
 * Created by lior.perry on 3/6/2018.
 */
public interface RefreshableStatisticsProviderFactory extends StatisticsProviderFactory{
    void refresh();
    String getSetup();
}
