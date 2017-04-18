package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.epb.plan.cost.calculation.CostCalculationUtil;
import com.kayhut.fuse.epb.plan.statistics.*;
import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;

import java.util.Optional;

/**
 * Created by moti on 01/04/2017.
 */
public class PlanOpStatisticsCostEstimator implements PlanOpCostEstimator<SingleCost> {
    private StatisticsProvider<StatisticableQueryItemInfo> statisticsProvider;

    @Override
    public SingleCost estimateCost(Optional<Plan<SingleCost>> plan, PlanOpBase planOpBase) {
        StatisticableOntologyElementInfo entityInfo;
        if(planOpBase instanceof EntityOp) {
            EntityOp entityOp = (EntityOp) planOpBase;
            entityInfo = new StatisticableOntologyElementInfo(entityOp.getEntity().geteBase());
            CardinalityStatistics cardinalityStatistics = statisticsProvider.getCardinalityStatistics(entityInfo);
            return CostCalculationUtil.calculateCostForCardinality(cardinalityStatistics);
        }


        if(planOpBase instanceof EntityFilterOp){
            EntityFilterOp entityFilterOp = (EntityFilterOp) planOpBase;
            entityInfo = new StatisticableOntologyElementInfo(entityFilterOp.getEprop().geteBase());
            HistogramStatistics<?> histogramStatistics = statisticsProvider.getHistogramStatistics(entityInfo);

        }


        return null;

    }

}
