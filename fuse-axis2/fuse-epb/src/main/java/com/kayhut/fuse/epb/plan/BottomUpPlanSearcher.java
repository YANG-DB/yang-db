package com.kayhut.fuse.epb.plan;

import com.codahale.metrics.Slf4jReporter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;
import com.kayhut.fuse.dispatcher.utils.NDC;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.estimation.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.planTree.BuilderIfc;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.log.TraceComposite;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;


/**
 * Created by moti on 2/21/2017.
 */
public class BottomUpPlanSearcher<P extends IPlan, C extends Cost, Q extends IQuery> implements PlanSearcher<P, C, Q> {
    @Inject
    public BottomUpPlanSearcher(PlanExtensionStrategy<P, Q> extensionStrategy,
                                @Named("GlobalPruningStrategy") PlanPruneStrategy<PlanWithCost<P, C>> globalPruneStrategy,
                                @Named("LocalPruningStrategy") PlanPruneStrategy<PlanWithCost<P, C>> localPruneStrategy,
                                @Named("GlobalPlanSelector") PlanSelector<PlanWithCost<P, C>, Q> globalPlanSelector,
                                @Named("LocalPlanSelector") PlanSelector<PlanWithCost<P, C>, Q> localPlanSelector,
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
    @LoggerAnnotation(name = "search", options = LoggerAnnotation.Options.returnValue, logLevel = Slf4jReporter.LoggingLevel.INFO)
    public Iterable<PlanWithCost<P, C>> search(Q query) {
        Iterable<PlanWithCost<P, C>> selectedPlans;

        // Generate seed plans (plan is null)
        final IncrementalEstimationContext<P, C, Q> estimationContext = new IncrementalEstimationContext<>(Optional.empty(), query);
        List<PlanWithCost<P, C>> currentPlans =
                Stream.ofAll(this.extensionStrategy.extendPlan(Optional.empty(), query))
                .filter(seedPlan -> this.planValidator.isPlanValid(seedPlan, query).valid())
                .map(validSeedPlan -> this.costEstimator.estimate(validSeedPlan, estimationContext))
                .toJavaList();

        selectedPlans = localPlanSelector.select(query, currentPlans);

        // As long as we have search options, branch the search tree
        while (currentPlans.size() > 0) {
            List<PlanWithCost<P, C>> newPlans = new ArrayList<>();
            for (PlanWithCost<P, C> partialPlan : currentPlans) {
                final IncrementalEstimationContext<P, C, Q> partialEstimationContext = new IncrementalEstimationContext<>(Optional.of(partialPlan), query);
                Stream.ofAll(this.localPruneStrategy.prunePlans(
                Stream.ofAll(this.extensionStrategy.extendPlan(Optional.of(partialPlan.getPlan()), query))
                        .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid())
                        .map(validExtendedPlan -> this.costEstimator.estimate(validExtendedPlan, partialEstimationContext))))
                        .forEach(newPlans::add);
            }

            currentPlans = Stream.ofAll(this.globalPruneStrategy.prunePlans(newPlans)).toJavaList();
            selectedPlans = Stream.ofAll(selectedPlans).appendAll(this.localPlanSelector.select(query, currentPlans)).toJavaList();
        }

        return this.globalPlanSelector.select(query, selectedPlans);
    }
    //endregion
    //region Logger
    //endregion

}
