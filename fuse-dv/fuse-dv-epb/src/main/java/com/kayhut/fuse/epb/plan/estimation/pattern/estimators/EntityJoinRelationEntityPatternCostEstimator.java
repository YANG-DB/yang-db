package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.EntityJoinEntityPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

/**
 * Created by moti on 29/05/2017.
 */
public class EntityJoinRelationEntityPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Constructors
    public EntityJoinRelationEntityPatternCostEstimator(
            EntityRelationEntityPatternCostEstimator entityRelationEntityPatternCostEstimator) {
        this.entityRelationEntityPatternCostEstimator = entityRelationEntityPatternCostEstimator;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public Result<Plan, CountEstimatesCost> estimate(
            Pattern pattern,
            IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        if (!EntityJoinEntityPattern.class.isAssignableFrom(pattern.getClass())) {
            return EmptyResult.get();
        }

        EntityJoinEntityPattern entityJoinEntityPattern = (EntityJoinEntityPattern) pattern;

        Result<Plan, CountEstimatesCost> result =
                this.entityRelationEntityPatternCostEstimator.estimate(entityJoinEntityPattern, context);

        return Result.of(
                result.countsUpdateFactors(),
                context.getPreviousCost().get().getCost().getPlanStepCost(entityJoinEntityPattern.getEntityJoinOp()).get(),
                result.getPlanStepCosts().get(1),
                result.getPlanStepCosts().get(2));
    }
    //endregion

    //region Fields
    private EntityRelationEntityPatternCostEstimator entityRelationEntityPatternCostEstimator;
    //endregion
}
