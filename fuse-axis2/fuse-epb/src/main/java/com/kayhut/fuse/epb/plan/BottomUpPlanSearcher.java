package com.kayhut.fuse.epb.plan;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.descriptor.QueryDescriptor;
import com.kayhut.fuse.dispatcher.logging.FuseLoggerFactory;
import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.cost.CostEstimator;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.log.TraceComposite;
import javaslang.Tuple2;
import javaslang.collection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import static com.codahale.metrics.MetricRegistry.name;
import static com.codahale.metrics.Timer.Context;


/**
 * Created by moti on 2/21/2017.
 */
public class BottomUpPlanSearcher<P, C, Q> implements PlanSearcher<P, C, Q>, Trace<String> {
    private TraceComposite<String> trace = TraceComposite.build(this.getClass().getSimpleName());

    @Inject(optional = true)
    private FuseLoggerFactory<BottomUpPlanSearcherLogger<P,C,Q>> loggerFactory;

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
        generateDummyLogFactory();
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
        BottomUpPlanSearcherLogger<P, C, Q> logger = loggerFactory.getLogger();
        logger.logStartSearch(query);
        Iterable<PlanWithCost<P, C>> selectedPlans;

        Set<PlanWithCost<P, C>> currentPlans = new TreeSet<>(Comparator.comparing(o -> o.getPlan().toString()));

        // Generate seed plans (plan is null)
        for (P seedPlan : extensionStrategy.extendPlan(Optional.empty(), query)) {
            ValidationContext planValid = planValidator.isPlanValid(seedPlan, query);
            if (planValid.valid()) {
                PlanWithCost<P, C> planWithCost = costEstimator.estimate(seedPlan, Optional.empty(), query);
                currentPlans.add(planWithCost);
                logger.validPlan(0, seedPlan);
            } else {
                logger.notValid(0, seedPlan, planValid);
            }
        }

        logger.logPreSelectedPlans(0,currentPlans);
        selectedPlans = localPlanSelector.select(query, currentPlans);
        logger.logSelectedPlans(0,currentPlans);

        int phase = 1;
        // As long as we have search options, branch the search tree
        while (currentPlans.size() > 0) {
            Set<PlanWithCost<P, C>> newPlans = new HashSet<>();
            javaslang.collection.Set<PlanWithCost<P, C>> planExtensionsSet = javaslang.collection.HashSet.of();
            for (PlanWithCost<P, C> partialPlan : currentPlans) {
                Set<PlanWithCost<P, C>> planExtensions = new HashSet<>();
                for (P extendedPlan : extensionStrategy.extendPlan(Optional.of(partialPlan.getPlan()), query)) {
                    ValidationContext planValid = planValidator.isPlanValid(extendedPlan, query);
                    if (planValid.valid()) {
                        PlanWithCost<P, C> planWithCost = costEstimator.estimate(extendedPlan, Optional.of(partialPlan), query);
                        planExtensions.add(planWithCost);
                        logger.validPlan(phase,extendedPlan);
                        planExtensionsSet = planExtensionsSet.add(planWithCost);
                    } else {
                        logger.notValid(phase,extendedPlan, planValid);
                    }
                }

                for (PlanWithCost<P, C> planWithCost : localPruneStrategy.prunePlans(planExtensions)) {
                    newPlans.add(planWithCost);
                    logger.prunePlan(phase,planWithCost);
                }
                phase++;
            }

            logger.prune(phase,planExtensionsSet.diff(javaslang.collection.HashSet.ofAll(newPlans)));
            currentPlans.clear();

            for (PlanWithCost<P, C> planWithCost : globalPruneStrategy.prunePlans(newPlans)) {
                currentPlans.add(planWithCost);
            }
            selectedPlans = Stream.ofAll(selectedPlans).appendAll(localPlanSelector.select(query, currentPlans)).toJavaList();
        }

        logger.logPreSelectedPlans(phase, selectedPlans);
        selectedPlans = globalPlanSelector.select(query, selectedPlans);
        logger.logSelectedPlans(phase, selectedPlans);
        return selectedPlans;
    }
    private void generateDummyLogFactory() {
        this.loggerFactory = () -> new BottomUpPlanSearcherLogger<P, C, Q>() {
            @Override
            public void logStartSearch(Q q) {
            }

            @Override
            public void logSelectedPlans(int phase, Iterable<PlanWithCost<P, C>> plans) {
            }

            @Override
            public void notValid(int phase, P seedPlan, ValidationContext planValid) {
            }

            @Override
            public void validPlan(int phase, P seedPlan) {
            }

            @Override
            public void prunePlan(int phase, PlanWithCost<P, C> planWithCost) {
            }

            @Override
            public void prune(int phase, javaslang.collection.Set elements) {
            }

            @Override
            public void logPreSelectedPlans(int phase, Iterable<PlanWithCost<P, C>> selectedPlans) {
            }
        };
    }
    //endregion
    //region Logger
    public interface BottomUpPlanSearcherLogger<P,C,Q>{
        void logStartSearch(Q q);
        void logSelectedPlans(int phase, Iterable<PlanWithCost<P, C>> plans);
        void notValid(int phase, P seedPlan, ValidationContext planValid);
        void validPlan(int phase, P seedPlan);
        void prunePlan(int phase, PlanWithCost<P, C> planWithCost);
        void prune(int phase, javaslang.collection.Set elements);
        void logPreSelectedPlans(int phase, Iterable<PlanWithCost<P, C>> selectedPlans);
    }

    private static class BottomUpPlanSearcherLoggerImpl<P, C, Q> implements BottomUpPlanSearcherLogger<P,C,Q>{
        private final Logger logger = LoggerFactory.getLogger(BottomUpPlanSearcher.class);
        private Context time;
        private QueryDescriptor<Q> queryDescriptor;


        public BottomUpPlanSearcherLoggerImpl(QueryDescriptor<Q> queryDescriptor, MetricRegistry registry) {
            this.queryDescriptor = queryDescriptor;
            this.registry = registry;
        }

        private MetricRegistry registry;

        public void logStartSearch(Q q) {
            String pattern = queryDescriptor.getPattern(q);
            time = registry.timer(name(BottomUpPlanSearcher.class, queryDescriptor.getName(q))).time();
            logger.info("Starting Plan Search " + pattern);
        }

        public void logSelectedPlans(int phase, Iterable<PlanWithCost<P, C>> plans) {
            int i=0;
            for (PlanWithCost<P, C> plan : plans) {
                logger.info(String.format("Selected plan[%d] from search = %s", i++, plan.getPlan().toString()));
            }
        }

        public void notValid(int phase, P seedPlan, ValidationContext planValid) {
        }

        public void validPlan(int phase, P seedPlan) {

        }

        public void prunePlan(int phase, PlanWithCost<P, C> planWithCost) {

        }

        public void prune(int phase, javaslang.collection.Set elements) {

        }

        public void logPreSelectedPlans(int phase, Iterable<PlanWithCost<P, C>> selectedPlans) {

        }

    }
    //endregion

}
