package com.kayhut.fuse.epb.plan.estimation.step.estimators;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.step.*;
import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepCostEstimatorContext;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class M1StepCostEstimator extends CompositeStepCostEstimator<Plan, CountEstimatesCost, M1StepCostEstimatorContext> {
    //region Static
    private static Map<Class<? extends Step>,
            StepCostEstimator<Plan, CountEstimatesCost, M1StepCostEstimatorContext>> estimators(CostEstimationConfig config) {
        Map<Class<? extends Step>, StepCostEstimator<Plan, CountEstimatesCost, M1StepCostEstimatorContext>> estimators =
                new HashMap<>();

        estimators.put(EntityStep.class, new EntityStepCostEstimator());
        estimators.put(EntityRelationEntityStep.class, new EntityRelationEntityStepCostEstimator(config));
        estimators.put(GoToEntityRelationEntityStep.class, new GoToEntityRelationEntityStepCostEstimator(config));

        return estimators;
    }
    //endregion

    //region Constructors
    @Inject
    public M1StepCostEstimator(CostEstimationConfig config) {
        super(estimators(config));
    }

    @Inject
    public M1StepCostEstimator(double alpha, double delta) {
        super(estimators(new CostEstimationConfig(alpha, delta)));
    }
    //endregion
}
