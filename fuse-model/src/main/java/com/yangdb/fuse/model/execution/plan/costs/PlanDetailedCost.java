package com.yangdb.fuse.model.execution.plan.costs;

/*-
 *
 * PlanDetailedCost.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import com.yangdb.fuse.model.execution.plan.*;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 20/04/2017.
 */
public class PlanDetailedCost implements Cost, Cloneable {
    public PlanDetailedCost() {
    }

    public PlanDetailedCost(DoubleCost globalCost, Iterable<PlanWithCost<Plan, CountEstimatesCost>> planStepCosts) {
        this.globalCost = globalCost;
        this.planStepCosts = planStepCosts;
    }

    public PlanDetailedCost(PlanDetailedCost previousCost) {
        //todo implement clone
        this(previousCost.globalCost, previousCost.planStepCosts);
    }
    //region properties

    public DoubleCost getGlobalCost() {
        return globalCost;
    }

    public List<PlanOp> getPlanOps() {
        if (planStepCosts != null) {
            return Stream.ofAll(planStepCosts).flatMap(pc -> Stream.ofAll(pc.getPlan().getOps())).toJavaList();
        }

        return Collections.emptyList();
    }

    public Iterable<PlanWithCost<Plan, CountEstimatesCost>> getPlanStepCosts() {
        return planStepCosts;
    }

    public Optional<PlanWithCost<Plan, CountEstimatesCost>> getPlanStepCost(PlanOp planOp) {
        Optional<PlanWithCost<Plan, CountEstimatesCost>> opCost = Stream.ofAll(planStepCosts).filter(pc -> pc.getPlan().getOps().contains(planOp)).toJavaOptional();
        if(!opCost.isPresent()){
            for (EntityJoinOp entityJoinOp : Stream.ofAll(planStepCosts).flatMap(plan -> plan.getPlan().getOps()).filter(op -> op instanceof EntityJoinOp).map(op -> (EntityJoinOp) op)) {
                PlanWithCost<Plan, CountEstimatesCost> joinCost = getPlanStepCost(entityJoinOp).get();
                opCost = ((JoinCost)joinCost.getCost()).getLeftBranchCost().getPlanStepCost(planOp);
                if(opCost.isPresent()){
                    break;
                }
                opCost = ((JoinCost)joinCost.getCost()).getRightBranchCost().getPlanStepCost(planOp);
                if(opCost.isPresent()){
                    break;
                }
            }
        }
        return opCost;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new PlanDetailedCost(new DoubleCost(this.globalCost.cost), Stream.ofAll(this.planStepCosts).map(p -> {
            try {
                return new PlanWithCost<>(p.getPlan(), (CountEstimatesCost)p.getCost().clone());
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }).toJavaList());
    }

    //endregion

    //region Fields
    //default cost
    private DoubleCost globalCost = DoubleCost.of(0); ;
    private Iterable<PlanWithCost<Plan, CountEstimatesCost>> planStepCosts = Collections.EMPTY_LIST;
    //endregion


    @Override
    public String toString() {
        return " { " +
                "plan:" + new Plan(getPlanOps()).toString() + "," + "\n" +
                "estimation:" + (globalCost != null ? globalCost.toString() + "\n" : "")
                + " } ";
    }
}
