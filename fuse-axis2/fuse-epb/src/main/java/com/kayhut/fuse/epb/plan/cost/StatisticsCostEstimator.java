package com.kayhut.fuse.epb.plan.cost;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.EBase;

import java.util.Optional;

/**
 * Created by moti on 01/04/2017.
 */
public class StatisticsCostEstimator<C> implements CostEstimator<Plan, PlanDetailedCost> {
    private StatisticsProvider<EBase> statisticsProvider;

    @Inject
    public StatisticsCostEstimator(StatisticsProvider<EBase> statisticsProvider) {
        this.statisticsProvider = statisticsProvider;
    }

    @Override
    public PlanWithCost<Plan, PlanDetailedCost> estimate(Plan plan, Optional<PlanDetailedCost> previousCost) {
        PlanOpBase planOpBase = plan.getOps().get(plan.getOps().size() - 1);

        EBase eBase = null;
        if(planOpBase instanceof EntityOp) {
            EntityOp entityOp = (EntityOp) planOpBase;
            eBase = entityOp.getEntity().geteBase();
        }
        if(planOpBase instanceof RelationOp){
            RelationOp relationOp = (RelationOp) planOpBase;
            eBase = relationOp.getRelation().geteBase();
        }

        if(planOpBase instanceof EntityFilterOp){
            EntityFilterOp entityFilterOp = (EntityFilterOp) planOpBase;
            eBase = entityFilterOp.getEprop().geteBase();
        }
        if(eBase != null) {
            Statistics statistics = statisticsProvider.getStatistics(eBase);
            //todo use statistics to implement the additional cost combiner
            return null;
        }else {
            return null;
        }
    }
}
