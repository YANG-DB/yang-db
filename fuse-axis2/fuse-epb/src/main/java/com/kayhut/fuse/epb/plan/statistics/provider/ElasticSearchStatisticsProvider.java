package com.kayhut.fuse.epb.plan.statistics.provider;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.RawGraphStatisticableItemInfo;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import org.elasticsearch.client.Client;

/**
 * Created by moti on 4/20/2017.
 */
public class ElasticSearchStatisticsProvider implements StatisticsProvider<RawGraphStatisticableItemInfo> {
    private ElasticGraphConfiguration configuration;
    private Client client;

    @Inject
    public ElasticSearchStatisticsProvider(ElasticGraphConfiguration configuration, Client client) {
        this.configuration = configuration;
        this.client = client;
    }

    @Override
    public Statistics getStatistics(RawGraphStatisticableItemInfo item) {
        //todo return statistics based on elastic search configuration
        return other -> new Statistics.CardinalityStatistics(1,100);
    }
}
