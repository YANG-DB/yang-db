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
    Statistics.Cardinality getNodeStatistics(EEntityBase item);

    /**
     *
     * @param item
     * @param entityFilter
     * @return
     */
    Statistics.Cardinality getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter);

    /**
     *
     * @param item
     * @return
     */
    Statistics.Cardinality getEdgeStatistics(Rel item);

    /**
     *
     * @param item
     * @param entityFilter
     * @return
     */
    Statistics.Cardinality getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter);

    /**
     *
     * @param rel
     * @param entityFilter
     * @param direction
     * @return
     */
    Statistics.Cardinality getRedundantEdgeStatistics(Rel rel, RelPropGroup relPropGroup,EBase entity, EPropGroup entityFilter, Direction direction);

    /**
     *
     * @param rel
     * @param entity
     * @param entityFilter
     * @param direction
     * @return
     */
    Statistics.Cardinality getRedundantNodeStatistics(Rel rel, EEntityBase entity, EPropGroup entityFilter, Direction direction);

    /**
     * get avarage number of eadges per entity (by label context)
     * @return
     */
    long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) ;

}
