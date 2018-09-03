package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;

/**
 * Created by lior.perry on 3/6/2018.
 */
public final class NoStatsProvider implements StatisticsProviderFactory{
    @Override
    public StatisticsProvider get(Ontology ontology) {
        return new StatisticsProvider() {
            @Override
            public Statistics.SummaryStatistics getNodeStatistics(EEntityBase item) {
                return null;
            }

            @Override
            public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter) {
                return null;
            }

            @Override
            public Statistics.SummaryStatistics getEdgeStatistics(Rel item, EEntityBase source) {
                return null;
            }

            @Override
            public Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter, EEntityBase source) {
                return null;
            }

            @Override
            public Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup) {
                return null;
            }

            @Override
            public long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) {
                return 0;
            }
        };
    }
}
