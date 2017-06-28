package com.kayhut.fuse.epb.plan.cost.calculation;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class M1StepCostEstimator extends CompositeStepCostEstimator {
    //region Static
    private static Map<StatisticsCostEstimator.StatisticsCostEstimatorPatterns, PatternCostEstimator> patternEstimators(CostEstimationConfig config) {
        FullStepPatternEstimator fullStepPatternEstimator = new FullStepPatternEstimator(config);
        SingleEntityPatternEstimator singleEntityPatternEstimator = new SingleEntityPatternEstimator();
        GoToPatternEstimator goToPatternEstimator = new GoToPatternEstimator(config);

        Map<StatisticsCostEstimator.StatisticsCostEstimatorPatterns, PatternCostEstimator> map = new HashMap<>();
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
