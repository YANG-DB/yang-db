package com.kayhut.fuse.epb.plan.estimation.pattern;

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
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import java.util.function.Predicate;

/**
 * Created by lior.perry on 2/19/2018.
 */
public class PredicateCostEstimator<P, C, TContext> implements CostEstimator<P, C, TContext> {
    public final static String planPredicateParameter = "PredicateCostEstimator.@planPredicate";
    public final static String trueCostEstimatorParameter = "PredicateCostEstimator.@trueCostEstimator";
    public final static String falseCostEstimatorParameter = "PredicateCostEstimator.@falseCostEstimator";

    //region Constructors
    @Inject
    public PredicateCostEstimator(
            @Named(planPredicateParameter) Predicate<P> planPredicate,
            @Named(trueCostEstimatorParameter) CostEstimator<P, C, TContext> trueCostEstimator,
            @Named(falseCostEstimatorParameter) CostEstimator<P, C, TContext> falseCostEstimator) {
        this.planPredicate = planPredicate;
        this.trueCostEstimator = trueCostEstimator;
        this.falseCostEstimator = falseCostEstimator;
    }
    //endregion

    //region CostEstimator Implementation
    @Override
    public PlanWithCost<P, C> estimate(P plan, TContext context) {
        return this.planPredicate.test(plan) ?
                this.trueCostEstimator.estimate(plan, context) :
                this.falseCostEstimator.estimate(plan, context);
    }
    //endregion

    //region Fields
    private Predicate<P> planPredicate;
    private CostEstimator<P, C, TContext> trueCostEstimator;
    private CostEstimator<P, C, TContext> falseCostEstimator;
    //endregion
}
