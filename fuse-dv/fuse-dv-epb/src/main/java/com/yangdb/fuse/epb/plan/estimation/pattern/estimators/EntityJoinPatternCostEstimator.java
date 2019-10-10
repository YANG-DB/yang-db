package com.yangdb.fuse.epb.plan.estimation.pattern.estimators;

/*-
 *
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.yangdb.fuse.dispatcher.epb.CostEstimator;
import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.yangdb.fuse.epb.plan.estimation.pattern.EntityJoinPattern;
import com.yangdb.fuse.epb.plan.estimation.pattern.Pattern;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.yangdb.fuse.model.execution.plan.costs.JoinCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;

import java.util.Optional;

/**
 * Estimates the cost of a join pattern.
 * There are two main cases:
 *  1. A new join op
 *  2. An existing join op that has been extended
 */
public class EntityJoinPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {

    //TODO: decompose method to two separate methods each handing a join scenario (new join, and ongoing join)
    @Override
    public Result<Plan, CountEstimatesCost> estimate(Pattern pattern, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        EntityJoinPattern entityJoinPattern = (EntityJoinPattern) pattern;
        PlanDetailedCost leftBranchCost = null;
        PlanDetailedCost rightBranchCost = null;
        boolean newJoin = true;

        // Check if our plan is an extension of an existing join
        if(context.getPreviousCost().get().getPlan().getOps().get(0) instanceof EntityJoinOp){
            EntityJoinOp previousJoinOp = (EntityJoinOp)context.getPreviousCost().get().getPlan().getOps().get(0);
            // If previous join and current join have same left branches - this is an extension
            if(previousJoinOp.getLeftBranch().equals(entityJoinPattern.getEntityJoinOp().getLeftBranch())){
                newJoin = false;
                JoinCost previousJoinCost = (JoinCost) context.getPreviousCost().get().getCost().getPlanStepCost(previousJoinOp).get().getCost();
                try {
                    leftBranchCost = (PlanDetailedCost) previousJoinCost.getLeftBranchCost().clone();
                } catch (CloneNotSupportedException e) {
                    leftBranchCost = previousJoinCost.getLeftBranchCost();
                }
                PlanWithCost<Plan, PlanDetailedCost> rightPlanWithCostOld = new PlanWithCost<>(previousJoinOp.getRightBranch(), previousJoinCost.getRightBranchCost());
                rightBranchCost = costEstimator.estimate(entityJoinPattern.getEntityJoinOp().getRightBranch(),
                                                new IncrementalEstimationContext<>(Optional.of(rightPlanWithCostOld), context.getQuery())).getCost();
            }
        }

        if(newJoin) {
            if(context.getPreviousCost().get().getPlan().equals(entityJoinPattern.getEntityJoinOp().getLeftBranch())) {
                try {
                    leftBranchCost = (PlanDetailedCost) context.getPreviousCost().get().getCost().clone();
                } catch (CloneNotSupportedException e) {
                    leftBranchCost =  context.getPreviousCost().get().getCost();
                }
            }else{
                leftBranchCost = costEstimator.estimate(entityJoinPattern.getEntityJoinOp().getLeftBranch(), context).getCost();
            }
            rightBranchCost = costEstimator.estimate(entityJoinPattern.getEntityJoinOp().getRightBranch(), new IncrementalEstimationContext<>(Optional.empty(), context.getQuery())).getCost();
        }



        double countEstimate = calcJoinCounts(leftBranchCost, rightBranchCost, entityJoinPattern.getEntityJoinOp());
        double[] countsFactors = calcCountsFactors(countEstimate, leftBranchCost, rightBranchCost, entityJoinPattern.getEntityJoinOp());

        return PatternCostEstimator.Result.of(countsFactors,
                new PlanWithCost<>(new Plan(entityJoinPattern.getEntityJoinOp()),
                        new JoinCost(calcJoinCost(leftBranchCost,rightBranchCost, entityJoinPattern.getEntityJoinOp()),
                                countEstimate, leftBranchCost, rightBranchCost)));
    }

    private double[] calcCountsFactors(double joinCountEstimates, PlanDetailedCost leftBranchCost, PlanDetailedCost rightBranchCost, EntityJoinOp joinOp) {
        if(!joinOp.isComplete())
            return null;
        else if(joinCountEstimates == 0) {
            return new double[]{0.0,0.0};
        }
        else {
            PlanWithCost<Plan, CountEstimatesCost> leftOpCost = leftBranchCost.getPlanStepCost(PlanUtil.last(joinOp.getLeftBranch(), EntityOp.class).get()).get();
            EntityOp rightBranchLastEntityOp = PlanUtil.last(joinOp.getRightBranch(), EntityOp.class).get();
            PlanWithCost<Plan, CountEstimatesCost> rightOpCost = rightBranchCost.getPlanStepCost(rightBranchLastEntityOp).get();
            return new double[]{
                    joinCountEstimates / leftOpCost.getCost().peek() ,
                    joinCountEstimates / rightOpCost.getCost().peek()
            };
        }
    }

    private double calcJoinCost(PlanDetailedCost leftCost, PlanDetailedCost rightCost, EntityJoinOp joinOp){
        PlanWithCost<Plan, CountEstimatesCost> leftOpCost = leftCost.getPlanStepCost(PlanUtil.last(joinOp.getLeftBranch(), EntityOp.class).get()).get();
        EntityOp rightBranchLastEntityOp = PlanUtil.last(joinOp.getRightBranch(), EntityOp.class).get();
        if(joinOp.isComplete())
            return leftOpCost.getCost().peek() + rightCost.getPlanStepCost(rightBranchLastEntityOp).get().getCost().peek();
        else
            return 0;
    }

    private double calcJoinCounts(PlanDetailedCost leftCost, PlanDetailedCost rightCost, EntityJoinOp joinOp){
        PlanWithCost<Plan, CountEstimatesCost> leftOpCost = leftCost.getPlanStepCost(PlanUtil.last(joinOp.getLeftBranch(), EntityOp.class).get()).get();
        EntityOp rightBranchLastEntityOp = PlanUtil.last(joinOp.getRightBranch(), EntityOp.class).get();
        if(joinOp.isComplete())
            return Math.min(leftOpCost.getCost().peek() , rightCost.getPlanStepCost(rightBranchLastEntityOp).get().getCost().peek());
        else
            return 0;
    }

    public void setCostEstimator(CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator) {
        this.costEstimator = costEstimator;
    }

    private CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator;
}
