package com.kayhut.fuse.epb.plan;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.asg.AsgQueryTransformer;
import com.kayhut.fuse.asg.strategy.propertyGrouping.EPropGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.propertyGrouping.Quant1AllQuantGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.propertyGrouping.Quant1PropertiesGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.propertyGrouping.RelPropGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.selection.DefaultRelationSelectionAsgStrategy;
import com.kayhut.fuse.asg.strategy.selection.DefaultSelectionAsgStrategy;
import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.epb.plan.query.AsgUnionSplitQueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.UnionOp;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.resourceInfo.FuseError;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UnionPlanSearcher implements PlanSearcher<Plan, PlanDetailedCost, AsgQuery> {
    public static final String planSearcherParameter = "UnionPlanSearcher.@planSearcherParameter";
    private AsgUnionSplitQueryTransformer splitQueryTransformer;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> mainPlanSearcher;

    @Inject
    public UnionPlanSearcher(@Named(planSearcherParameter) PlanSearcher<Plan, PlanDetailedCost, AsgQuery> mainPlanSearcher, OntologyProvider ontologyProvider ) {
        this.mainPlanSearcher = mainPlanSearcher;
        final AsgQueryTransformer transformer = new AsgQueryTransformer(() -> Arrays.asList(
                new Quant1AllQuantGroupingAsgStrategy(),
                new EPropGroupingAsgStrategy(),
                new Quant1PropertiesGroupingAsgStrategy(),
                new RelPropGroupingAsgStrategy(),
                new DefaultSelectionAsgStrategy(ontologyProvider),
                new DefaultRelationSelectionAsgStrategy(ontologyProvider)),
                ontologyProvider );
        this.splitQueryTransformer = new AsgUnionSplitQueryTransformer(transformer);
    }

    @Override
    public PlanWithCost<Plan, PlanDetailedCost> search(AsgQuery query) {
        // generate multiple union plans (each plan is free from some-quant)
        final Iterable<AsgQuery> queries = splitQueryTransformer.transform(query);

        //plan main query
        final List<PlanWithCost<Plan, PlanDetailedCost>> plans = Stream.ofAll(queries).map(q -> mainPlanSearcher.search(q)).toJavaList();

        if (!Stream.ofAll(plans).filter(Objects::isNull).isEmpty()) {
            throw new IllegalStateException("UnionPlanSearcher - One of the plans is empty");
        }

        if (plans.size() == 1) {
            return plans.get(0);
        }

        //use UnionOp to collect all resulted plans

        final Stream<PlanWithCost<Plan, PlanDetailedCost>> stream = Stream.ofAll(plans)
                .filter(Objects::nonNull)
                .filter(p->p.getCost()!=null && p.getCost().getGlobalCost()!=null);

        final double sumCosts = (double) stream.map(p -> p.getCost().getGlobalCost().getCost()).sum();
        final List<PlanWithCost<Plan, CountEstimatesCost>> costs = stream
                .map(p -> new PlanWithCost<>(p.getPlan(), new CountEstimatesCost(p.getCost().getGlobalCost().getCost(), 0)))
                .toJavaList();

        final PlanDetailedCost planDetailedCost = new PlanDetailedCost(new DoubleCost(sumCosts), costs);
        final PlanWithCost<Plan, PlanDetailedCost> unionPlan = new PlanWithCost<>(new Plan(), planDetailedCost);
        //add unionOp to union plan
        unionPlan.getPlan().getOps().add(new UnionOp(stream.map(p -> p.getPlan().getOps()).toJavaList()));

        return unionPlan;
    }


}
