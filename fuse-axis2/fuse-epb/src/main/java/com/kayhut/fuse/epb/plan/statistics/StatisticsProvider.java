package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
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
    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getNodeStatistics(EEntityBase item);

    /**
     *
     * @param item
     * @param entityFilter
     * @return
     */
    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter);

    /**
     *
     * @param item
     * @return
     */
    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getEdgeStatistics(Rel item);

    /**
     *
     * @param item
     * @param entityFilter
     * @return
     */
    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter);

    /**
     *
     * @param rel
     * @param entityFilter
     * @param direction
     * @return
     */
    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getRedundantEdgeStatistics(Rel rel, EBase entity, EPropGroup entityFilter, Direction direction);

    /**
     *
     * @param rel
     * @param entity
     * @param entityFilter
     * @param direction
     * @return
     */
    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getRedundantNodeStatistics(Rel rel, EBase entity, EPropGroup entityFilter, Direction direction);

    /**
     * get avarage number of eadges per node (by label context)
     * @return
     */
    long getGlobalSelectivity(Rel rel, EBase entity, Direction direction) ;

}
