package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

import com.kayhut.fuse.epb.plan.estimation.pattern.GoToEntityRelationEntityPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

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

        CountEstimatesCost gotoCost = new CountEstimatesCost(0, 0);

        return PatternCostEstimator.Result.of(
                result.lambda(),
                new PlanWithCost<>(new Plan(goToEntityRelationEntityPattern.getStartGoTo()), gotoCost),
                result.getPlanStepCosts().get(1),
                result.getPlanStepCosts().get(2));
    }
    //endregion

    //region Fields
    private EntityRelationEntityPatternCostEstimator entityRelationEntityPatternCostEstimator;
    //endregion
}
