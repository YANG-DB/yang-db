package com.kayhut.fuse.epb.plan.cost;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.cost.calculation.CostCalculator;
import com.kayhut.fuse.epb.plan.statistics.*;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.query.EBase;

import java.util.Optional;

/**
 * Created by moti on 01/04/2017.
 */
public class PlanOpStatisticsCostEstimator<C> implements PlanOpCostEstimator<C> {
    private StatisticsProvider<EBase> statisticsProvider;
    private CostCalculator<C, Statistics, PlanOpBase> opCostCalculator;
    private CostCalculator<C, C,Plan<C>> costCombiner;

    @Inject
    public PlanOpStatisticsCostEstimator(StatisticsProvider<EBase> statisticsProvider,
                                         CostCalculator<C, Statistics, PlanOpBase> opCostCalculator,
                                         CostCalculator<C, C, Plan<C>> costCombiner) {
        this.statisticsProvider = statisticsProvider;
        this.opCostCalculator = opCostCalculator;
        this.costCombiner = costCombiner;
    }

    @Override
    public C estimateCost(Optional<Plan<C>> plan, PlanOpBase planOpBase) {
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
            C opCost = opCostCalculator.calculateCost(statisticsProvider.getStatistics(eBase), Optional.of(planOpBase));
            return costCombiner.calculateCost(opCost, plan);
        }else {
            return null;
        }

    }

}
