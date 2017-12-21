package com.kayhut.fuse.dispatcher.epb;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.Optional;

/**
 * Created by Roman on 12/16/2017.
 */
public class PlanTracer {

    //region Builder
    public static class Builder {
        //region Constructors
        public Builder() {

        }
        //endregion
    }
    //endregion

    //region Searcher
    public static class Searcher<P, C, Q> implements PlanSearcher<P, C, Q> {
        public static final String planSearcherParameter = "PlanTracer.Searcher.@planSearcherParameter";

        //region Constructors
        @Inject
        public Searcher(
                @Named(planSearcherParameter) PlanSearcher<P, C, Q> planSearcher,
                String planSearcherName,
                PlanTracer.Builder builder) {
            this.planSearcher = planSearcher;
            this.planSearcherName = planSearcherName;
            this.builder = builder;
        }
        //endregion

        //region PlanSearcher Implementation
        @Override
        public PlanWithCost<P, C> search(Q query) {
            return this.planSearcher.search(query);
        }
        //endregion

        //region Fields
        private PlanSearcher<P, C, Q> planSearcher;
        private String planSearcherName;
        private PlanTracer.Builder builder;
        //endregion

        //region Provider
        public static class Provider<P, C, Q> implements com.google.inject.Provider<PlanSearcher<P, C, Q>> {
            public static final String planSearcherParameter = "PlanTracer.Searcher.Provider.@planSearcherParameter";

            //region Constructors
            @Inject
            public Provider(
                    @Named(planSearcherParameter) PlanSearcher<P, C, Q> planSearcher,
                    String planSearcherName,
                    PlanTracer.Builder builder,
                    CreateQueryRequest createQueryRequest) {
                this.planSearcher = planSearcher;
                this.planSearcherName = planSearcherName;
                this.builder = builder;
                this.isVerbose = createQueryRequest.isVerbose();
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanSearcher<P, C, Q> get() {
                return isVerbose ?
                        new Searcher<>(this.planSearcher, this.planSearcherName, this.builder) :
                        this.planSearcher;
            }
            //endregion

            //region Fields
            private PlanSearcher<P, C, Q> planSearcher;
            private String planSearcherName;
            private PlanTracer.Builder builder;
            private boolean isVerbose;
            //endregion
        }
        //endregion
    }
    //endregion

    //region ExtensionStrategy
    public static class ExtensionStrategy<P extends IPlan, Q extends IQuery> implements PlanExtensionStrategy<P, Q> {
        public static final String planExtensionStrategyParameter = "PlanTracer.ExtensionStrategy.@planExtensionStrategy";

        //region Constructors
        @Inject
        public ExtensionStrategy(
                @Named(planExtensionStrategyParameter) PlanExtensionStrategy<P, Q> planExtensionStrategy,
                String planExtensionStrategyName,
                PlanTracer.Builder builder) {
            this.planExtensionStrategy = planExtensionStrategy;
            this.planExtensionStrategyName = planExtensionStrategyName;
            this.builder = builder;
        }
        //endregion

        //region PlanExtensionStrategy Implementation
        @Override
        public Iterable<P> extendPlan(Optional<P> plan, Q query) {
            return this.planExtensionStrategy.extendPlan(plan, query);
        }
        //endregion

        //region Fields
        private PlanExtensionStrategy<P, Q> planExtensionStrategy;
        private String planExtensionStrategyName;
        private PlanTracer.Builder builder;
        //endregion

        //region Provider
        public static class Provider<P extends IPlan, Q extends IQuery> implements com.google.inject.Provider<PlanExtensionStrategy<P, Q>> {
            public static final String planExtensionStrategyParameter = "PlanTracer.ExtensionStrategy.Provider.@planExtensionStrategy";

            //region Constructors
            @Inject
            public Provider(
                    @Named(planExtensionStrategyParameter) PlanExtensionStrategy<P, Q> planExtensionStrategy,
                    String planExtensionStrategyName,
                    PlanTracer.Builder builder,
                    CreateQueryRequest createQueryRequest) {
                this.planExtensionStrategy = planExtensionStrategy;
                this.planExtensionStrategyName = planExtensionStrategyName;
                this.builder = builder;
                this.isVerbose = createQueryRequest.isVerbose();
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanExtensionStrategy<P, Q> get() {
                return isVerbose ?
                        new ExtensionStrategy<>(this.planExtensionStrategy, this.planExtensionStrategyName, this.builder) :
                        this.planExtensionStrategy;
            }
            //endregion

            //region Fields
            private PlanExtensionStrategy<P, Q> planExtensionStrategy;
            private String planExtensionStrategyName;
            private PlanTracer.Builder builder;
            private boolean isVerbose;
            //endregion
        }
        //endregion
    }
    //endregion

    //region Validator
    public static class Validator<P, Q> implements PlanValidator<P, Q> {
        public static final String planValidatorParameter = "PlanTracer.Validator.@planValidator";

        //region Constructors
        @Inject
        public Validator(
                @Named(planValidatorParameter) PlanValidator<P, Q> planValidator,
                String planValidatorName,
                PlanTracer.Builder builder) {
            this.planValidator = planValidator;
            this.planValidatorName = planValidatorName;
            this.builder = builder;
        }
        //endregion

        //region PlanValidator Implementation
        @Override
        public ValidationResult isPlanValid(P plan, Q query) {
            return this.planValidator.isPlanValid(plan, query);
        }
        //endregion

        //region Fields
        private PlanValidator<P, Q> planValidator;
        private String planValidatorName;
        private PlanTracer.Builder builder;
        //endregion

        //region Provider
        public static class Provider<P extends IPlan, Q extends IQuery> implements com.google.inject.Provider<PlanValidator<P, Q>> {
            public static final String planValidatorParameter = "PlanTracer.Validator.Provider.@planValidator";

            //region Constructors
            @Inject
            public Provider(
                    @Named(planValidatorParameter) PlanValidator<P, Q> planValidator,
                    String planValidatorName,
                    PlanTracer.Builder builder,
                    CreateQueryRequest createQueryRequest) {
                this.planValidator = planValidator;
                this.planValidatorName = planValidatorName;
                this.builder = builder;
                this.isVerbose = createQueryRequest.isVerbose();
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanValidator<P, Q> get() {
                return isVerbose ?
                        new Validator<>(this.planValidator, this.planValidatorName, this.builder) :
                        this.planValidator;
            }
            //endregion

            //region Fields
            private PlanValidator<P, Q> planValidator;
            private String planValidatorName;
            private PlanTracer.Builder builder;
            private boolean isVerbose;
            //endregion
        }
        //endregion
    }
    //endregion

    //region Estimator
    public static class Estimator<P, C, TContext> implements CostEstimator<P, C, TContext> {
        public static final String costEstimatorParameter = "PlanTracer.Estimator.@costEstimator";

        //region Constructors
        @Inject
        public Estimator(
                @Named(costEstimatorParameter) CostEstimator<P, C, TContext> costEstimator,
                String costEstimatorName,
                PlanTracer.Builder builder) {
            this.costEstimator = costEstimator;
            this.builder = builder;
        }
        //endregion

        //region CostEstimator Implementation
        @Override
        public PlanWithCost<P, C> estimate(P plan, TContext context) {
            return this.costEstimator.estimate(plan, context);
        }
        //endregion

        //region Fields
        private CostEstimator<P, C, TContext> costEstimator;
        private PlanTracer.Builder builder;
        //endregion

        //region Provider
        public static class Provider<P extends IPlan, C extends Cost, TContext> implements com.google.inject.Provider<CostEstimator<P, C, TContext>> {
            public static final String costEstimatorParameter = "PlanTracer.Estimator.Provider.@costEstimator";

            //region Constructors
            @Inject
            public Provider(
                    @Named(costEstimatorParameter) CostEstimator<P, C, TContext> costEstimator,
                    PlanTracer.Builder builder,
                    CreateQueryRequest createQueryRequest) {
                this.costEstimator = costEstimator;
                this.builder = builder;
                this.isVerbose = createQueryRequest.isVerbose();
            }
            //endregion

            //region Provider Implementation
            @Override
            public CostEstimator<P, C, TContext> get() {
                return isVerbose ?
                        new Estimator<>(this.costEstimator, "", this.builder) :
                        this.costEstimator;
            }
            //endregion

            //region Fields
            private CostEstimator<P, C, TContext> costEstimator;
            private PlanTracer.Builder builder;
            private boolean isVerbose;
            //endregion
        }
        //endregion

        //region Pruner
        public static class PruneStrategy<P, C> implements PlanPruneStrategy<PlanWithCost<P, C>> {
            public static final String planPruneStrategyParameter = "PlanTracer.PruneStrategy.@planPruneStrategy";

            //region Constructors
            @Inject
            public PruneStrategy(
                    @Named(planPruneStrategyParameter) PlanPruneStrategy<PlanWithCost<P, C>> planPruneStrategy,
                    String planPruneStrategyName,
                    PlanTracer.Builder builder) {
                this.planPruneStrategy = planPruneStrategy;
                this.planPruneStrategyName = planPruneStrategyName;
                this.builder = builder;
            }
            //endregion

            //region PlanPruneStrategy Implementation
            @Override
            public Iterable<PlanWithCost<P, C>> prunePlans(Iterable<PlanWithCost<P, C>> plans) {
                return this.planPruneStrategy.prunePlans(plans);
            }
            //endregion

            //region Fields
            private PlanPruneStrategy<PlanWithCost<P, C>> planPruneStrategy;
            private String planPruneStrategyName;
            private PlanTracer.Builder builder;
            //endregion

            //region Provider
            public static class Provider<P, C> implements com.google.inject.Provider<PlanPruneStrategy<PlanWithCost<P, C>>> {
                public static final String planPruneStrategyParameter = "PlanTracer.PruneStrategy.Provider.@planPruneStrategy";

                //region Constructors
                public Provider(
                        @Named(planPruneStrategyParameter) PlanPruneStrategy<PlanWithCost<P, C>> planPruneStrategy,
                        String planPruneStrategyName,
                        PlanTracer.Builder builder,
                        CreateQueryRequest createQueryRequest) {
                    this.planPruneStrategy = planPruneStrategy;
                    this.planPruneStrategyName = planPruneStrategyName;
                    this.builder = builder;
                    this.isVerbose = createQueryRequest.isVerbose();
                }
                //endregion

                //region Provider Implementation
                @Override
                public PlanPruneStrategy<PlanWithCost<P, C>> get() {
                    return isVerbose ?
                            new PruneStrategy<>(this.planPruneStrategy, this.planPruneStrategyName, this.builder) :
                            this.planPruneStrategy;
                }
                //endregion

                //region Fields
                private PlanPruneStrategy<PlanWithCost<P, C>> planPruneStrategy;
                private String planPruneStrategyName;
                private PlanTracer.Builder builder;
                private boolean isVerbose;
                //endregion
            }
            //endregion
        }
        //endregion
    }
    //endregion
}
