package com.kayhut.fuse.epb.plan.cost;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.CostEstimator;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by moti on 01/04/2017.
 */
public class StatisticsCostEstimator<C> implements CostEstimator<C> {
    private StatisticsProvider<EBase> statisticsProvider;
    private CostEstimator<C> costEstimator;

    @Inject
    public StatisticsCostEstimator(StatisticsProvider<EBase> statisticsProvider,
                                   CostEstimator<C> costEstimator) {
        this.statisticsProvider = statisticsProvider;
        this.costEstimator = costEstimator;
    }

    @Override
    public C estimateCost(Plan<C> plan, PlanOpBase planOpBase) {
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
            return costEstimator.estimateCost(planOpBase);
        }else {
            return null;
        }

    }

    @Override
    public C estimateCost(PlanOpBase planOpBase) {
        return costEstimator.estimateCost(planOpBase);
    }

    @Override
    public C estimateCost(Plan<C> plan) {
        return costEstimator.estimateCost(plan);
    }

}
