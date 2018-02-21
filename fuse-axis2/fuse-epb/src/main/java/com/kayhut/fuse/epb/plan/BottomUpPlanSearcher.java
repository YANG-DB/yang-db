package com.kayhut.fuse.epb.plan;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.dispatcher.epb.*;
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import javaslang.collection.Stream;

import java.util.*;


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


    //region Fields
    private PlanExtensionStrategy<P, Q> extensionStrategy;
    private PlanPruneStrategy<PlanWithCost<P, C>> globalPruneStrategy;
    private PlanPruneStrategy<PlanWithCost<P, C>> localPruneStrategy;
    private PlanSelector<PlanWithCost<P, C>, Q> globalPlanSelector;
    private PlanSelector<PlanWithCost<P, C>, Q> localPlanSelector;
    private PlanValidator<P, Q> planValidator;
    private CostEstimator<P, C, IncrementalEstimationContext<P, C, Q>> costEstimator;
    //endregion

    //region Methods
    @Override
    public PlanWithCost<P, C> search(Q query) {
        Iterable<PlanWithCost<P, C>> selectedPlans;

        // Generate seed plans (plan is null)
        final IncrementalEstimationContext<P, C, Q> estimationContext = new IncrementalEstimationContext<>(Optional.empty(), query);
        //filter initial plans
        Stream<P> filteredPlans = Stream.ofAll(this.extensionStrategy.extendPlan(Optional.empty(), query)).filter(seedPlan -> this.planValidator.isPlanValid(seedPlan, query).valid());
        if(filteredPlans.size()==0)
            throw new IllegalStateException("Initial plan generation, Filter stage - no valid plan was found for query "+(AsgQueryDescriptor.toString((AsgQuery) query)));
        //local prune initial valid plans + cost estimation
        List<PlanWithCost<P, C>> currentPlans = Stream.ofAll(this.localPruneStrategy.prunePlans(filteredPlans.map(validSeedPlan -> this.costEstimator.estimate(validSeedPlan, estimationContext)))).toJavaList();
        //select plans
        selectedPlans = localPlanSelector.select(query, currentPlans);

        // As long as we have search options, branch the search tree
        while (currentPlans.size() > 0) {
            List<PlanWithCost<P, C>> newPlans = new ArrayList<>();
            for (PlanWithCost<P, C> partialPlan : currentPlans) {
                final IncrementalEstimationContext<P, C, Q> partialEstimationContext = new IncrementalEstimationContext<>(Optional.of(partialPlan), query);
                Stream<P> filter = Stream.ofAll(this.extensionStrategy.extendPlan(Optional.of(partialPlan.getPlan()), query))
                        .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid());
                //local prune initial valid plans + cost estimation
                Stream.ofAll(this.localPruneStrategy.prunePlans(filter.map(validExtendedPlan ->
                        this.costEstimator.estimate(validExtendedPlan, partialEstimationContext)))).forEach(newPlans::add);
            }

            currentPlans = Stream.ofAll(this.globalPruneStrategy.prunePlans(newPlans)).toJavaList();
            selectedPlans = Stream.ofAll(selectedPlans).appendAll(this.localPlanSelector.select(query, currentPlans)).toJavaList();
        }

        if(!Stream.ofAll(this.globalPlanSelector.select(query, selectedPlans)).isEmpty())
            return Stream.ofAll(this.globalPlanSelector.select(query, selectedPlans)).get(0);

        throw new IllegalStateException("No valid plan was found for query "+(AsgQueryDescriptor.toString((AsgQuery) query)));
    }
    //endregion
}
