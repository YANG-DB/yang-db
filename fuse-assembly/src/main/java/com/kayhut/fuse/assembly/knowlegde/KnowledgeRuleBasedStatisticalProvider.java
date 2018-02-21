package com.kayhut.fuse.assembly.knowlegde;

import com.kayhut.fuse.epb.plan.statistics.RuleBasedStatisticalProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProviderFactory;
import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.google.inject.Inject;

/**
 * Created by lior.perry on 2/18/2018.
 */
public class KnowledgeRuleBasedStatisticalProvider implements StatisticsProviderFactory {
    @Inject
    public KnowledgeRuleBasedStatisticalProvider() {}

    @Override
    public StatisticsProvider get(Ontology ontology) {
        return new RuleBasedStatisticalProvider() {
            @Override
            public Statistics.SummaryStatistics getNodeStatistics(EEntityBase item) {
                return new Statistics.SummaryStatistics(1,1);
            }

            @Override
            public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter) {
                return new Statistics.SummaryStatistics(1,1);
            }

            @Override
            public Statistics.SummaryStatistics getEdgeStatistics(Rel item) {
                return new Statistics.SummaryStatistics(1,1);
            }

            @Override
            public Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter) {
                return new Statistics.SummaryStatistics(1,1);
            }

            @Override
            public Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup) {
                return new Statistics.SummaryStatistics(1,1);
            }

            @Override
            public long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) {
                return 0;
            }
        };
    }
}
