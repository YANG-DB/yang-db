package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.EntityJoinPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.JoinCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import javaslang.collection.Stream;

import java.util.Optional;

public class EntityJoinPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {


    @Override
    public Result<Plan, CountEstimatesCost> estimate(Pattern pattern, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        EntityJoinPattern entityJoinPattern = (EntityJoinPattern) pattern;
        if(context.getPreviousCost().get().getPlan().getOps().get(0) instanceof EntityJoinOp){
            EntityJoinOp planOp = (EntityJoinOp)context.getPreviousCost().get().getPlan().getOps().get(0);
            if(planOp.getLeftBranch().equals(entityJoinPattern.getEntityJoinOp().getLeftBranch())){
                JoinCost joinCost = (JoinCost) context.getPreviousCost().get().getCost().getPlanStepCost(planOp).get().getCost();
                PlanWithCost<Plan, PlanDetailedCost> rightPlanWithCostOld = new PlanWithCost<>(planOp.getRightBranch(), joinCost.getRightBranchCost());
                PlanWithCost<Plan, PlanDetailedCost> rightPlanWithCostNew = costEstimator.estimate(entityJoinPattern.getEntityJoinOp().getRightBranch(),
                                                new IncrementalEstimationContext<>(Optional.of(rightPlanWithCostOld), context.getQuery()));
                return PatternCostEstimator.Result.of(1.0,
                        new PlanWithCost<>(new Plan(entityJoinPattern.getEntityJoinOp()),
                                                    new JoinCost(calcCost(joinCost.getLeftBranchCost(),rightPlanWithCostNew.getCost(), planOp),
                                                            calcCounts(joinCost.getLeftBranchCost(), rightPlanWithCostNew.getCost(), planOp),
                                                            joinCost.getLeftBranchCost(), rightPlanWithCostNew.getCost())));
            }
        }

        PlanWithCost<Plan, PlanDetailedCost> leftCost = context.getPreviousCost().get();
        PlanWithCost<Plan, PlanDetailedCost> rightPlanWithCost = costEstimator.estimate(entityJoinPattern.getEntityJoinOp().getRightBranch(), new IncrementalEstimationContext<>(Optional.empty(), context.getQuery()));
        return PatternCostEstimator.Result.of(1.0,
                new PlanWithCost<>(new Plan(entityJoinPattern.getEntityJoinOp()),
                        new JoinCost(calcCost(leftCost.getCost(),rightPlanWithCost.getCost(), entityJoinPattern.getEntityJoinOp()),
                                calcCounts(leftCost.getCost(),rightPlanWithCost.getCost(), entityJoinPattern.getEntityJoinOp()), leftCost.getCost(), rightPlanWithCost.getCost())));
    }

    private double calcCost(PlanDetailedCost leftCost, PlanDetailedCost rightCost, EntityJoinOp entityOp){
        PlanWithCost<Plan, CountEstimatesCost> leftOpCost = leftCost.getPlanStepCost(leftCost.getPlanOps().stream().filter(op -> op instanceof EntityOp && ((EntityOp) op).getAsgEbase().equals(entityOp.getAsgEbase())).findFirst().get()).get();
        Optional<PlanOp> rightEntityOp = rightCost.getPlanOps().stream().filter(op -> op instanceof EntityOp && ((EntityOp) op).getAsgEbase().equals(entityOp.getAsgEbase())).findFirst();
        if(rightEntityOp.isPresent())
            return leftOpCost.getCost().peek() + rightCost.getPlanStepCost(rightEntityOp.get()).get().getCost().peek();
        else
            return 0;
    }

    private double calcCounts(PlanDetailedCost leftCost, PlanDetailedCost rightCost, EntityJoinOp entityOp){
        PlanWithCost<Plan, CountEstimatesCost> leftOpCost = leftCost.getPlanStepCost(leftCost.getPlanOps().stream().filter(op -> op instanceof EntityOp && ((EntityOp) op).getAsgEbase().equals(entityOp.getAsgEbase())).findFirst().get()).get();
        Optional<PlanOp> rightEntityOp = rightCost.getPlanOps().stream().filter(op -> op instanceof EntityOp && ((EntityOp) op).getAsgEbase().equals(entityOp.getAsgEbase())).findFirst();
        if(rightEntityOp.isPresent())
            return Math.min(leftOpCost.getCost().peek() , rightCost.getPlanStepCost(rightEntityOp.get()).get().getCost().peek());
        else
            return 0;
    }

    public void setCostEstimator(CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator) {
        this.costEstimator = costEstimator;
    }

    private CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator;
}
