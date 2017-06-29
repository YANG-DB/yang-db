package com.kayhut.fuse.epb.plan.estimation.step;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.step.context.M1StepCostEstimatorContext;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.EntityRelationEntityStepCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.GoToEntityRelationEntityStepCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.EntityStepCostEstimator;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;

import java.util.Arrays;

/**
 * Created by moti on 29/05/2017.
 */
public class M1StepCostEstimator extends CompositeStepCostEstimator {
    //region Static
    private static Iterable<StepCostEstimator<Plan, CountEstimatesCost, M1StepCostEstimatorContext>> estimators(CostEstimationConfig config) {
        return Arrays.asList(
                new EntityRelationEntityStepCostEstimator(config),
                new EntityStepCostEstimator(),
                new GoToEntityRelationEntityStepCostEstimator(config)
        );
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
