package com.kayhut.fuse.epb.plan.cost;

import com.kayhut.fuse.epb.plan.statistics.HistogramStatistics;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.StatisticableFieldInfo;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;

import java.util.Optional;

/**
 * Created by moti on 01/04/2017.
 */
public class PlanOpStatisticsCostEstimator implements PlanOpCostEstimator<SingleCost> {
    StatisticsProvider<HistogramStatistics<?>, StatisticableFieldInfo> fieldStatisticsProvider;

    @Override
    public SingleCost estimateCost(Optional<Plan<SingleCost>> plan, PlanOpBase planOpBase) {
        return null;
    }
}
