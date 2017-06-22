package com.kayhut.fuse.epb.plan;

import com.codahale.metrics.Slf4jReporter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;
import com.kayhut.fuse.dispatcher.utils.NDC;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.cost.CostEstimator;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.ICost;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.log.TraceComposite;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.slf4j.MDC;

import java.util.*;
import java.util.logging.Level;


/**
 * Created by moti on 2/21/2017.
 */
public class BottomUpPlanSearcher<P extends IPlan, C extends ICost, Q extends IQuery> implements PlanSearcher<P, C, Q>, Trace<String> {
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
    @LoggerAnnotation(name = "search", logLevel = Slf4jReporter.LoggingLevel.INFO)
    public Iterable<PlanWithCost<P, C>> search(Q query) {
        Iterable<PlanWithCost<P, C>> selectedPlans;

        Set<PlanWithCost<P, C>> currentPlans = new TreeSet<>(Comparator.comparing(o -> o.getPlan().toString()));

        // Generate seed plans (plan is null)
        for (P seedPlan : extensionStrategy.extendPlan(Optional.empty(), query)) {
            ValidationContext planValid = planValidator.isPlanValid(seedPlan, query);
            if (planValid.valid()) {
                PlanWithCost<P, C> planWithCost = costEstimator.estimate(seedPlan, Optional.empty(), query);
                currentPlans.add(planWithCost);
            }
        }

        selectedPlans = localPlanSelector.select(query, currentPlans);

        int phase = 1;
        // As long as we have search options, branch the search tree
        while (currentPlans.size() > 0) {
            Set<PlanWithCost<P, C>> newPlans = new HashSet<>();
            javaslang.collection.Set<PlanWithCost<P, C>> planExtensionsSet = javaslang.collection.HashSet.of();
            for (PlanWithCost<P, C> partialPlan : currentPlans) {
                try {
                    NDC.push("phase:" + Integer.toString(phase));
                    Set<PlanWithCost<P, C>> planExtensions = new HashSet<>();
                    for (P extendedPlan : extensionStrategy.extendPlan(Optional.of(partialPlan.getPlan()), query)) {
                        ValidationContext planValid = planValidator.isPlanValid(extendedPlan, query);
                        if (planValid.valid()) {
                            PlanWithCost<P, C> planWithCost = costEstimator.estimate(extendedPlan, Optional.of(partialPlan), query);
                            planExtensions.add(planWithCost);
                            planExtensionsSet = planExtensionsSet.add(planWithCost);
                        }
                    }

                    for (PlanWithCost<P, C> planWithCost : localPruneStrategy.prunePlans(planExtensions)) {
                        newPlans.add(planWithCost);
                    }
                    phase++;
                }finally {
                    NDC.pop();
                }
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
    //region Logger
    //endregion

}
