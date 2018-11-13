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
import com.kayhut.fuse.dispatcher.epb.*;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * Created by moti on 2/21/2017.
 */
public class BottomUpPlanSearcher<P extends IPlan, C extends Cost, Q extends IQuery> implements PlanSearcher<P, C, Q> {
    public static final String globalPruneStrategyParameter = "BottomUpPlanSearcher.@globalPruneStrategy";
    public static final String localPruneStrategyParameter = "BottomUpPlanSearcher.@localPruneStrategy";
    public static final String globalPlanSelectorParameter = "BottomUpPlanSearcher.@globalPlanSelector";
    public static final String localPlanSelectorParameter = "BottomUpPlanSearcher.@localPlanSelector";

    @Inject
    public BottomUpPlanSearcher(PlanExtensionStrategy<P, Q> extensionStrategy,
                                @Named(globalPruneStrategyParameter) PlanPruneStrategy<PlanWithCost<P, C>> globalPruneStrategy,
                                @Named(localPruneStrategyParameter) PlanPruneStrategy<PlanWithCost<P, C>> localPruneStrategy,
                                @Named(globalPlanSelectorParameter) PlanSelector<PlanWithCost<P, C>, Q> globalPlanSelector,
                                @Named(localPlanSelectorParameter) PlanSelector<PlanWithCost<P, C>, Q> localPlanSelector,
                                PlanValidator<P, Q> planValidator,
                                CostEstimator<P, C, IncrementalEstimationContext<P, C, Q>> costEstimator) {
        this.extensionStrategy = extensionStrategy;
        this.globalPruneStrategy = globalPruneStrategy;
        this.localPruneStrategy = localPruneStrategy;
        this.globalPlanSelector = globalPlanSelector;
        this.localPlanSelector = localPlanSelector;
        this.planValidator = planValidator;
        this.costEstimator = costEstimator;
    }

    //region Methods
    @Override
    public PlanWithCost<P, C> search(Q query) {
        Iterable<PlanWithCost<P, C>> selectedPlans = Collections.emptyList();
        List<PlanWithCost<P, C>> currentPlans = Collections.singletonList(null);

        while (currentPlans.size() > 0) {
            List<PlanWithCost<P, C>> newPlans = new ArrayList<>();
            for (PlanWithCost<P, C> partialPlan : currentPlans) {
                final IncrementalEstimationContext<P, C, Q> context = new IncrementalEstimationContext<>(Optional.ofNullable(partialPlan), query);
                Stream.ofAll(this.localPruneStrategy.prunePlans(
                    Stream.ofAll(this.extensionStrategy.extendPlan(context.getPreviousCost().map(PlanWithCost::getPlan), query))
                            .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid())
                            .map(validExtendedPlan -> this.costEstimator.estimate(validExtendedPlan, context))))
                        .forEach(newPlans::add);
            }

            currentPlans = Stream.ofAll(this.globalPruneStrategy.prunePlans(newPlans)).toJavaList();
            selectedPlans = Stream.ofAll(selectedPlans).appendAll(this.localPlanSelector.select(query, currentPlans)).toJavaList();
        }

        selectedPlans = this.globalPlanSelector.select(query, selectedPlans);
        return Stream.ofAll(selectedPlans).isEmpty() ? null : Stream.ofAll(selectedPlans).get(0);
    }
    //endregion

    //region Fields
    private PlanExtensionStrategy<P, Q> extensionStrategy;
    private PlanPruneStrategy<PlanWithCost<P, C>> globalPruneStrategy;
    private PlanPruneStrategy<PlanWithCost<P, C>> localPruneStrategy;
    private PlanSelector<PlanWithCost<P, C>, Q> globalPlanSelector;
    private PlanSelector<PlanWithCost<P, C>, Q> localPlanSelector;
    private PlanValidator<P, Q> planValidator;
    private CostEstimator<P, C, IncrementalEstimationContext<P, C, Q>> costEstimator;
    //endregion
}
