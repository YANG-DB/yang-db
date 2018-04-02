package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.driver.InternalsDriver;
import com.kayhut.fuse.epb.plan.statistics.RefreshableStatisticsProviderFactory;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;

import java.util.Optional;

/**
 * Created by lior on 20/02/2017.
 */
public class StandardInternalsDriver implements InternalsDriver {
    private StatisticsProviderFactory statisticsProviderFactory;

    //region Constructors
    @Inject
    public StandardInternalsDriver(StatisticsProviderFactory factory) {
        this.statisticsProviderFactory = factory;
    }

    //endregion
    @Override
    public Optional<String> getStatisticsProviderName() {
        return Optional.of(statisticsProviderFactory.getClass().getSimpleName());
    }

    @Override
    public Optional<String> getStatisticsProviderSetup() {
        if (statisticsProviderFactory instanceof RefreshableStatisticsProviderFactory)
            return Optional.of(((RefreshableStatisticsProviderFactory) statisticsProviderFactory).getSetup());
        return getStatisticsProviderName();
    }

    @Override
    public Optional<String> refreshStatisticsProviderSetup() {
        if (statisticsProviderFactory instanceof RefreshableStatisticsProviderFactory) {
            ((RefreshableStatisticsProviderFactory) statisticsProviderFactory).refresh();
            return getStatisticsProviderSetup();
        }
        return Optional.empty();
    }
    //endregion
}
