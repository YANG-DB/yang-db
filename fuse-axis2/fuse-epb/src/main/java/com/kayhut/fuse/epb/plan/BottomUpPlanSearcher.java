package com.kayhut.fuse.epb.plan;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.epb.plan.cost.CostEstimator;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 2/21/2017.
 */
public class BottomUpPlanSearcher<P, C, Q> implements PlanSearcher<P, C, Q> {
    final Logger logger = LoggerFactory.getLogger(BottomUpPlanSearcher.class);

    @Inject
    public BottomUpPlanSearcher(PlanExtensionStrategy<P, Q> extensionStrategy,
                                @Named("GlobalPruningStrategy") PlanPruneStrategy<PlanWithCost<P, C>> globalPruneStrategy,
                                @Named("LocalPruningStrategy") PlanPruneStrategy<PlanWithCost<P, C>> localPruneStrategy,
                                @Named("GlobalPlanSelector") PlanSelector<PlanWithCost<P, C>, Q> globalPlanSelector,
                                @Named("LocalPlanSelector") PlanSelector<PlanWithCost<P, C>, Q> localPlanSelector,
                                PlanValidator<P, Q> planValidator,
                                CostEstimator<P, C> costEstimator) {
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
    private CostEstimator<P, C> costEstimator;
    //endregion

    //region Methods
    @Override
    public Iterable<PlanWithCost<P, C>> search(Q query){
        Iterable<PlanWithCost<P, C>> selectedPlans = Collections.emptyList();

        List<PlanWithCost<P, C>> currentPlans = new LinkedList<>();

        // Generate seed plans (plan is null)
        for(P seedPlan : extensionStrategy.extendPlan(Optional.empty(), query)){
            if(planValidator.isPlanValid(seedPlan, query)){
                PlanWithCost<P, C> planWithCost = costEstimator.estimate(seedPlan, Optional.empty());
                currentPlans.add(planWithCost);
            }
        }

        selectedPlans = localPlanSelector.select(query, currentPlans);

        // As long as we have search options, branch the search tree
        while(currentPlans.size() > 0)
        {
            List<PlanWithCost<P, C>> newPlans = new LinkedList<>();
            for(PlanWithCost<P, C> partialPlan : currentPlans){
                List<PlanWithCost<P, C>> planExtensions = new LinkedList<>();
                for(P extendedPlan : extensionStrategy.extendPlan(Optional.of(partialPlan.getPlan()), query)){
                    if(planValidator.isPlanValid(extendedPlan, query)){
                        PlanWithCost<P, C> planWithCost = costEstimator.estimate(extendedPlan, Optional.of(partialPlan));
                        planExtensions.add(planWithCost);
                    }
                }

                for(PlanWithCost<P, C> planWithCost : localPruneStrategy.prunePlans(planExtensions))
                    newPlans.add(planWithCost);
            }

            currentPlans.clear();
            for(PlanWithCost<P, C> planWithCost : globalPruneStrategy.prunePlans(newPlans)){
                currentPlans.add(planWithCost);
            }

            selectedPlans = Stream.ofAll(selectedPlans).appendAll(localPlanSelector.select(query, currentPlans)).toJavaList();
        }

        selectedPlans = globalPlanSelector.select(query, selectedPlans);

        return selectedPlans;
    }

    //endregion
}
