package com.kayhut.fuse.epb.plan.estimation.step;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.step.context.StatisticsPatternContext;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.FullStepPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.GoToStepPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.SingleEntityStepPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.StepPatternCostEstimator;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class M1StepCostEstimator extends CompositeStepCostEstimator {
    //region Static
    private static Map<StatisticsCostEstimator.Pattern,
            StepPatternCostEstimator<Plan, PlanDetailedCost, CountEstimatesCost, StatisticsPatternContext>> patternEstimators(CostEstimationConfig config) {
        FullStepPatternCostEstimator fullStepPatternEstimator = new FullStepPatternCostEstimator(config);
        SingleEntityStepPatternCostEstimator singleEntityPatternEstimator = new SingleEntityStepPatternCostEstimator();
        GoToStepPatternCostEstimator goToPatternEstimator = new GoToStepPatternCostEstimator(config);

        Map<StatisticsCostEstimator.Pattern,
                StepPatternCostEstimator<Plan, PlanDetailedCost, CountEstimatesCost, StatisticsPatternContext>> estimators = new HashMap<>();
        estimators.put(StatisticsCostEstimator.Pattern.FULL_STEP, fullStepPatternEstimator);
        estimators.put(StatisticsCostEstimator.Pattern.SINGLE_MODE, singleEntityPatternEstimator);
        estimators.put(StatisticsCostEstimator.Pattern.GOTO_MODE, goToPatternEstimator);

        return estimators;
    }
    //endregion

    //region Constructors
    @Inject
    public M1StepCostEstimator(CostEstimationConfig config) {
        super(patternEstimators(config));
    }

    @Inject
    public M1StepCostEstimator(double alpha, double delta) {
        super(patternEstimators(new CostEstimationConfig(alpha, delta)));
    }
    //endregion
}
