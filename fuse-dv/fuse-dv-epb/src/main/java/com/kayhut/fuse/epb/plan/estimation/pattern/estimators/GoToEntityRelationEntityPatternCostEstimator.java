package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.kayhut.fuse.epb.plan.estimation.pattern.GoToEntityRelationEntityPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Stack;

/**
 * Created by moti on 29/05/2017.
 */
public class GoToEntityRelationEntityPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Constructors
    public GoToEntityRelationEntityPatternCostEstimator(
            EntityRelationEntityPatternCostEstimator entityRelationEntityPatternCostEstimator) {
        this.entityRelationEntityPatternCostEstimator = entityRelationEntityPatternCostEstimator;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public PatternCostEstimator.Result<Plan, CountEstimatesCost> estimate(
            Pattern pattern,
            IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        if (!GoToEntityRelationEntityPattern.class.isAssignableFrom(pattern.getClass())) {
            return PatternCostEstimator.EmptyResult.get();
        }

        GoToEntityRelationEntityPattern goToEntityRelationEntityPattern = (GoToEntityRelationEntityPattern) pattern;

        PatternCostEstimator.Result<Plan, CountEstimatesCost> result =
                this.entityRelationEntityPatternCostEstimator.estimate(goToEntityRelationEntityPattern, context);

        Stack<Double> counts = (Stack<Double>) result.getPlanStepCosts().get(0).getCost().getCountEstimates().clone();
        counts.pop();
        CountEstimatesCost gotoCost = new CountEstimatesCost(0, counts.peek());

        return PatternCostEstimator.Result.of(
                result.countsUpdateFactors(),
                new PlanWithCost<>(new Plan(goToEntityRelationEntityPattern.getStartGoTo()), gotoCost),
                result.getPlanStepCosts().get(1),
                result.getPlanStepCosts().get(2));
    }
    //endregion

    //region Fields
    private EntityRelationEntityPatternCostEstimator entityRelationEntityPatternCostEstimator;
    //endregion
}
