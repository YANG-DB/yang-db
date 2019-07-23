package com.yangdb.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.driver.InternalsDriver;
import com.yangdb.fuse.epb.plan.statistics.RefreshableStatisticsProviderFactory;
import com.yangdb.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.typesafe.config.Config;

import java.util.Optional;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class StandardInternalsDriver implements InternalsDriver {
    private Config config;
    private StatisticsProviderFactory statisticsProviderFactory;

    //region Constructors
    @Inject
    public StandardInternalsDriver(com.typesafe.config.Config config,StatisticsProviderFactory factory) {
        this.config = config;
        this.statisticsProviderFactory = factory;
    }

    @Override
    public Optional<Config> getConfig() {
        return Optional.of(config);
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
