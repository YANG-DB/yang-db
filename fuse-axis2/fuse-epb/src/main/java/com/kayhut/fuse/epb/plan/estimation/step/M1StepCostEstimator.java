package com.kayhut.fuse.epb.plan.estimation.step;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.estimation.StatisticsCostEstimator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class M1StepCostEstimator extends CompositeStepCostEstimator {
    //region Static
    private static Map<StatisticsCostEstimator.StatisticsCostEstimatorPatterns, StepPatternCostEstimator> patternEstimators(CostEstimationConfig config) {
        FullStepPatternCostEstimator fullStepPatternEstimator = new FullStepPatternCostEstimator(config);
        SingleEntityStepPatternCostEstimator singleEntityPatternEstimator = new SingleEntityStepPatternCostEstimator();
        GoToStepPatternCostEstimator goToPatternEstimator = new GoToStepPatternCostEstimator(config);

        Map<StatisticsCostEstimator.StatisticsCostEstimatorPatterns, StepPatternCostEstimator> map = new HashMap<>();
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorPatterns.FULL_STEP, fullStepPatternEstimator);
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorPatterns.SINGLE_MODE, singleEntityPatternEstimator);
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorPatterns.GOTO_MODE, goToPatternEstimator);

        return map;
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
