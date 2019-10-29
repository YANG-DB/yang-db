package com.yangdb.fuse.epb.plan.estimation.pattern;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
 * #L%
 */

import com.yangdb.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.yangdb.fuse.model.execution.plan.costs.DoubleCost;
import com.yangdb.fuse.model.execution.plan.costs.JoinCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by Roman on 29/06/2017.
 */
public class EntityJoinPattern extends Pattern {
    //region Constructors
    public EntityJoinPattern(EntityJoinOp entityJoinOp) {
        this.entityJoinOp = entityJoinOp;
    }
    //endregion

    @Override
    public PlanWithCost<Plan, PlanDetailedCost> buildNewPlan(PatternCostEstimator.Result<Plan, CountEstimatesCost> result, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        if(previousCost.isPresent()){
            Plan joinPlan = result.getPlanStepCosts().get(0).getPlan();
            JoinCost joinCost = (JoinCost) result.getPlanStepCosts().get(0).getCost();
            if( result.countsUpdateFactors()!= null){
                joinCost.applyCountsUpdateFactorOnLeftBranch(result.countsUpdateFactors()[0]);
                joinCost.applyCountsUpdateFactorOnRightBranch(result.countsUpdateFactors()[1]);
            }
            PlanDetailedCost planDetailedCost = new PlanDetailedCost(new DoubleCost(joinCost.getCost() + joinCost.getLeftBranchCost().getGlobalCost().cost + joinCost.getRightBranchCost().getGlobalCost().cost),
                    Arrays.asList(new PlanWithCost<>(joinPlan, joinCost)));
            return new PlanWithCost<>( joinPlan, planDetailedCost);
        }else{
            return super.buildNewPlan(result, previousCost);
        }
    }

    //region Properties

    public EntityJoinOp getEntityJoinOp() {
        return entityJoinOp;
    }

    //endregion

    //region Fields
    private EntityJoinOp entityJoinOp;

    //endregion
}
