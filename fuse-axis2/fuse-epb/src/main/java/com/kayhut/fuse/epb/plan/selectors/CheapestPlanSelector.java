package com.kayhut.fuse.epb.plan.selectors;

import com.codahale.metrics.Slf4jReporter;
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;
import com.kayhut.fuse.epb.plan.PlanSelector;
import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.Collections;

/**
 * Created by moti on 21/05/2017.
 */
public class CheapestPlanSelector implements PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> {
    @Override
    @LoggerAnnotation(name = "select", options = LoggerAnnotation.Options.full, logLevel = Slf4jReporter.LoggingLevel.DEBUG)
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> select(AsgQuery query, Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        PlanWithCost<Plan, PlanDetailedCost> minPlan = null;
        for(PlanWithCost<Plan, PlanDetailedCost> planWithCost : plans) {
            if(SimpleExtenderUtils.checkIfPlanIsComplete(planWithCost.getPlan(), query)) {
                if (minPlan == null)
                    minPlan = planWithCost;
                else {
                    if (minPlan.getCost().getGlobalCost().cost > planWithCost.getCost().getGlobalCost().cost)
                        minPlan = planWithCost;
                }
            }
        }

        return minPlan!= null ? Collections.singleton(minPlan): Collections.emptyList();
    }
}
