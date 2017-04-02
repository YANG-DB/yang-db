package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.epb.plan.cost.calculation.HistogramCalculations;
import com.kayhut.fuse.epb.plan.statistics.HistogramStatistics;
import com.kayhut.fuse.epb.plan.statistics.StatisticableEntityInfo;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.StatisticableItemInfo;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;

import java.util.Optional;

/**
 * Created by moti on 01/04/2017.
 */
public class PlanOpStatisticsCostEstimator implements PlanOpCostEstimator<SingleCost> {
    private StatisticsProvider<StatisticableItemInfo> fieldStatisticsProvider;

    @Override
    public SingleCost estimateCost(Optional<Plan<SingleCost>> plan, PlanOpBase planOpBase) {
        if(planOpBase instanceof EntityOp) {
            EntityOp entityOp = (EntityOp) planOpBase;
            StatisticableEntityInfo entityInfo = new StatisticableEntityInfo(entityOp.getEntity().geteBase());
            HistogramStatistics<String> statistics = fieldStatisticsProvider.getStatistics(entityInfo);
            String[] terms = new String[1];
            terms[0] = entityInfo.getEntity().geteTag();
            return HistogramCalculations.calculateTermsCost(statistics, terms);
        }

        return null;

    }

}
