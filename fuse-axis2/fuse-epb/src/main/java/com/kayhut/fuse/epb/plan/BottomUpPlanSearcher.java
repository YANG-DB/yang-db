package com.kayhut.fuse.epb.plan;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.epb.plan.cost.CostEstimator;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.log.TraceComposite;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by moti on 2/21/2017.
 */
public class BottomUpPlanSearcher<P, C, Q> implements PlanSearcher<P, C, Q>, Trace<String> {
    final Logger logger = LoggerFactory.getLogger(BottomUpPlanSearcher.class);
    private TraceComposite<String> trace = TraceComposite.build(this.getClass().getSimpleName());

    @Override
    public void log(String event, Level level) {
        trace.log(event, level);
    }

    @Override
    public List<Tuple2<String, String>> getLogs(Level level) {
        return trace.getLogs(level);
    }

    @Override
    public String who() {
        return trace.who();
    }

    @Inject
    public BottomUpPlanSearcher(PlanExtensionStrategy<P, Q> extensionStrategy,
                                @Named("GlobalPruningStrategy") PlanPruneStrategy<PlanWithCost<P, C>> globalPruneStrategy,
                                @Named("LocalPruningStrategy") PlanPruneStrategy<PlanWithCost<P, C>> localPruneStrategy,
                                @Named("GlobalPlanSelector") PlanSelector<PlanWithCost<P, C>, Q> globalPlanSelector,
                                @Named("LocalPlanSelector") PlanSelector<PlanWithCost<P, C>, Q> localPlanSelector,
                                PlanValidator<P, Q> planValidator,
                                CostEstimator<P, C, Q> costEstimator) {
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
    private CostEstimator<P, C, Q> costEstimator;
    //endregion

    //region Methods
    @Override
    public Iterable<PlanWithCost<P, C>> search(Q query) {
        Iterable<PlanWithCost<P, C>> selectedPlans;

        Set<PlanWithCost<P, C>> currentPlans = new TreeSet<>((o1, o2) -> o1.getPlan().toString().compareTo(o2.getPlan().toString()));

        // Generate seed plans (plan is null)
        for (P seedPlan : extensionStrategy.extendPlan(Optional.empty(), query)) {
            if (planValidator.isPlanValid(seedPlan, query)) {
                PlanWithCost<P, C> planWithCost = costEstimator.estimate(seedPlan, Optional.empty(), query);
                currentPlans.add(planWithCost);
            }
        }

        selectedPlans = localPlanSelector.select(query, currentPlans);

        int step = 0;
        // As long as we have search options, branch the search tree
        while (currentPlans.size() > 0) {
            Set<PlanWithCost<P, C>> newPlans = new HashSet<>();
            for (PlanWithCost<P, C> partialPlan : currentPlans) {
                Set<PlanWithCost<P, C>> planExtensions = new HashSet<>();
//                if (partialPlan != null) {
                    for (P extendedPlan : extensionStrategy.extendPlan(Optional.of(partialPlan.getPlan()), query)) {
                        log("Step#" + step + " [" + planValidator.isPlanValid(extendedPlan, query) + "]" + Plan.toPattern((Plan) extendedPlan), Level.INFO);
                        if (planValidator.isPlanValid(extendedPlan, query)) {
                            PlanWithCost<P, C> planWithCost = costEstimator.estimate(extendedPlan, Optional.of(partialPlan), query);
                            planExtensions.add(planWithCost);
//                        }
                    }
                }

                for (PlanWithCost<P, C> planWithCost : localPruneStrategy.prunePlans(planExtensions)) {
                    newPlans.add(planWithCost);
                }
                step++;
            }

            currentPlans.clear();
            for (PlanWithCost<P, C> planWithCost : globalPruneStrategy.prunePlans(newPlans)) {
                currentPlans.add(planWithCost);
            }
            selectedPlans = Stream.ofAll(selectedPlans).appendAll(localPlanSelector.select(query, currentPlans)).toJavaList();
        }

        selectedPlans = globalPlanSelector.select(query, selectedPlans);

        return selectedPlans;
    }

    //endregion
}
