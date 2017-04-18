package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.epb.plan.cost.calculation.CostCalculationUtil;
import com.kayhut.fuse.epb.plan.cost.calculation.CostCalculator;
import com.kayhut.fuse.epb.plan.statistics.*;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.Optional;

/**
 * Created by moti on 01/04/2017.
 */
public class PlanOpStatisticsCostEstimator implements PlanOpCostEstimator<SingleCost> {
    private StatisticsProvider<StatisticableQueryItemInfo> statisticsProvider;
    private CostCalculator<SingleCost, CardinalityStatistics, Plan<SingleCost>> costCalculator;

    @Override
    public SingleCost estimateCost(Optional<Plan<SingleCost>> plan, PlanOpBase planOpBase) {
        StatisticableOntologyElementInfo entityInfo;
        if(planOpBase instanceof EntityOp) {
            EntityOp entityOp = (EntityOp) planOpBase;
            entityInfo = new StatisticableOntologyElementInfo(entityOp.getEntity().geteBase());
            CardinalityStatistics cardinalityStatistics = statisticsProvider.getCardinalityStatistics(entityInfo);
            return costCalculator.calculateCost(cardinalityStatistics, plan);
        }
        if(planOpBase instanceof RelationOp){
            RelationOp relationOp = (RelationOp) planOpBase;
            entityInfo = new StatisticableOntologyElementInfo(relationOp.getRelation().geteBase());
            CardinalityStatistics cardinalityStatistics = statisticsProvider.getCardinalityStatistics(entityInfo);
            return costCalculator.calculateCost(cardinalityStatistics, plan);
        }

        if(planOpBase instanceof EntityFilterOp){
            EntityFilterOp entityFilterOp = (EntityFilterOp) planOpBase;
            entityInfo = new StatisticableOntologyElementInfo(entityFilterOp.getEprop().geteBase());
            HistogramStatistics<?> histogramStatistics = statisticsProvider.getHistogramStatistics(entityInfo);
            //return costCalculator.calculateCost(histogramStatistics, plan);
        }

        return null;

    }

}
