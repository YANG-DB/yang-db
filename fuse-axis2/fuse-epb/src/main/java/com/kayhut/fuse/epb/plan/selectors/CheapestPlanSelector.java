package com.kayhut.fuse.epb.plan.selectors;

import com.kayhut.fuse.dispatcher.epb.PlanSelector;
import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import javaslang.control.Option;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

/**
 * Created by moti on 21/05/2017.
 */
public class CheapestPlanSelector implements PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> {
    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> select(AsgQuery query, Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        return Stream.ofAll(plans).filter(plan -> SimpleExtenderUtils.checkIfPlanIsComplete(plan.getPlan(), query)).minBy((o1, o2) -> {
            if (o1.getCost().getGlobalCost().cost == o2.getCost().getGlobalCost().cost) {
                return Integer.compare(o1.getPlan().hashCode(), o2.getPlan().hashCode());
            }
            return Double.compare(o1.getCost().getGlobalCost().cost, o2.getCost().getGlobalCost().cost);

        }).toJavaList();
    }
}
