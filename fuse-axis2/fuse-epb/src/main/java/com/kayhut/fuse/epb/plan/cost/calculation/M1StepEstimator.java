package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class M1StepEstimator {

    public static StepEstimator getStepEstimator(double alpha,double delta){
        return getStepEstimator(new CostEstimationConfig(alpha,delta));
    }

    public static StepEstimator getStepEstimator(CostEstimationConfig config ){
        FullStepPatternEstimator fullStepPatternEstimator = new FullStepPatternEstimator(config);
        GoToPatternEstimator goToPatternEstimator = new GoToPatternEstimator(config);
        SingleEntityPatternEstimator singleEntityPatternEstimator = new SingleEntityPatternEstimator();
        Map<StatisticsCostEstimator.StatisticsCostEstimatorPatterns, PatternCostEstimator> map = new HashMap<>();
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorPatterns.SINGLE_MODE, singleEntityPatternEstimator);
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorPatterns.FULL_STEP, fullStepPatternEstimator);
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorPatterns.GOTO_MODE, goToPatternEstimator);
        return new CompositeStepCostEstimator(map);
    }
}
