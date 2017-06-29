package com.kayhut.fuse.epb.plan.estimation.step;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.FullStepPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.GoToStepPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.SingleEntityStepPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.step.pattern.StepPatternCostEstimator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class M1StepCostEstimator extends CompositeStepCostEstimator {
    //region Static
    private static Map<StatisticsCostEstimator.Pattern, StepPatternCostEstimator> patternEstimators(CostEstimationConfig config) {
        FullStepPatternCostEstimator fullStepPatternEstimator = new FullStepPatternCostEstimator(config);
        SingleEntityStepPatternCostEstimator singleEntityPatternEstimator = new SingleEntityStepPatternCostEstimator();
        GoToStepPatternCostEstimator goToPatternEstimator = new GoToStepPatternCostEstimator(config);

        Map<StatisticsCostEstimator.Pattern, StepPatternCostEstimator> map = new HashMap<>();
        map.put(StatisticsCostEstimator.Pattern.FULL_STEP, fullStepPatternEstimator);
        map.put(StatisticsCostEstimator.Pattern.SINGLE_MODE, singleEntityPatternEstimator);
        map.put(StatisticsCostEstimator.Pattern.GOTO_MODE, goToPatternEstimator);

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
