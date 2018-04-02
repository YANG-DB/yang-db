package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;

/**
 * Created by moti on 31/03/2017.
 */
public interface StatisticsProvider {
    /**
     *
     * @param item
     * @return
     */
    Statistics.SummaryStatistics getNodeStatistics(EEntityBase item);

    /**
     *
     * @param item
     * @param entityFilter
     * @return
     */
    Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter);

    /**
     *
     * @param item
     * @return
     */
    Statistics.SummaryStatistics getEdgeStatistics(Rel item);

    /**
     *
     * @param item
     * @param entityFilter
     * @return
     */
    Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter);


    //Statistics.SummaryStatistics getRedundantEdgeStatistics(Rel rel, RelPropGroup relPropGroup,DirectionSchema direction);


    Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup);

    /**
     * get average number of edges per node (by label context)
     * @return
     */
    long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) ;

}
