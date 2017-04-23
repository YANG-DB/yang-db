package com.kayhut.fuse.epb.plan.cost;

import com.google.inject.Inject;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 01/04/2017.
 */
public class StatisticsCostEstimator implements CostEstimator<Plan, PlanWithCost> {
    private StatisticsProvider<EBase> statisticsProvider;

    @Inject
    public StatisticsCostEstimator(StatisticsProvider<EBase> statisticsProvider) {
        this.statisticsProvider = statisticsProvider;
    }

    @Override
    public PlanWithCost<Plan, PlanWithCost> estimate(Plan plan, Optional<PlanWithCost<Plan, PlanWithCost>> previousCost) {
        List<PlanOpBase> step = plan.getOps();
        if (previousCost.isPresent()) {
//            step = extractNewSteps(plan, previousCost.get().getPlan());
        }


//        match(getSupportedPattern(),step)
        return null;
    }

    public List<String> getSupportedPattern() {
        /*
            entity->[filter]
            entity->[filter]->rel->[filter]->entity->[filter]
         */
        return Arrays.asList(
        //option 1
                "^(?<entityOnly>" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<optionalEntityOnlyFilter>" + EntityFilterOp.class.getSimpleName() + "))?$",
        //option2
                "^(?<entityOne>" + EntityOp.class.getSimpleName() + ")" + ":" + "(?<optionalEntityOneFilter>" + EntityFilterOp.class.getSimpleName() + ":)?" +
                        "(?<relation>" + RelationOp.class.getSimpleName() + ")" + ":" + "(?<optionalRelFilter>" + RelationFilterOp.class.getSimpleName() + ":)?" +
                        "(?<entityTwo>" + EntityOp.class.getSimpleName() + ")" + "(:" + "(?<optionalEntityTwoFilter>" + EntityFilterOp.class.getSimpleName() + "))?$");
    }
}