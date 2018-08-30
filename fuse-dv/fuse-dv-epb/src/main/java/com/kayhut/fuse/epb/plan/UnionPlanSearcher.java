package com.kayhut.fuse.epb.plan;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.asg.AsgQueryTransformer;
import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.epb.plan.query.AsgUnionSplitQueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.UnionOp;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;

import java.util.List;

public class UnionPlanSearcher implements PlanSearcher<Plan, PlanDetailedCost, AsgQuery> {
    public static final String bottomUpPlanSearcher = "UnionPlanSearcher.@bottomUpPlanSearcher";
    private AsgUnionSplitQueryTransformer splitQueryTransformer;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> mainPlanSearcher;

    @Inject
    public UnionPlanSearcher(@Named(bottomUpPlanSearcher) PlanSearcher<Plan, PlanDetailedCost, AsgQuery> mainPlanSearcher, QueryTransformer<AsgQuery, AsgQuery> transformer) {
        this.mainPlanSearcher = mainPlanSearcher;
        this.splitQueryTransformer = new AsgUnionSplitQueryTransformer(transformer);
    }

    @Override
    public PlanWithCost<Plan, PlanDetailedCost> search(AsgQuery query) {
        // generate multiple union plans (each plan is free from some-quant)
        final Iterable<AsgQuery> queries = splitQueryTransformer.transform(query);

        //plan main query
        final List<PlanWithCost<Plan, PlanDetailedCost>> plans = Stream.ofAll(queries).map(q -> mainPlanSearcher.search(q)).toJavaList();

        if (plans.size() == 1) {
            return mainPlanSearcher.search(query);
        }

        //use UnionOp to collect all resulted plans

        final double sumCosts = (double) Stream.ofAll(plans).map(p -> p.getCost().getGlobalCost().getCost()).sum();
        final List<PlanWithCost<Plan, CountEstimatesCost>> costs = Stream.ofAll(plans)
                .map(p -> new PlanWithCost<>(p.getPlan(), new CountEstimatesCost(p.getCost().getGlobalCost().getCost(), 0)))
                .toJavaList();

        final PlanDetailedCost planDetailedCost = new PlanDetailedCost(new DoubleCost(sumCosts), costs);
        final PlanWithCost<Plan, PlanDetailedCost> unionPlan = new PlanWithCost<>(new Plan(), planDetailedCost);
        //add unionOp to union plan
        unionPlan.getPlan().getOps().add(new UnionOp(Stream.ofAll(plans).map(p -> p.getPlan().getOps()).toJavaList()));

        return unionPlan;
    }


}
