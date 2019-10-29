package com.yangdb.fuse.dispatcher.epb;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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



import com.google.inject.*;
import com.google.inject.name.Named;
import com.yangdb.fuse.model.asgQuery.IQuery;
import com.yangdb.fuse.model.execution.plan.IPlan;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.transport.PlanTraceOptions;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by Roman on 12/16/2017.
 */
public class PlanTracer {

    //region Wrapper
    public static class Wrapper<P extends IPlan, C, CContext, Q extends IQuery>{
        //region Constructors
        public Wrapper(PlanTraceOptions planTraceOptions) {
            this.planTraceOptions = planTraceOptions;

            this.planSearcherClasses = new ArrayList<>();
            this.planExtensionStrategyClasses = new ArrayList<>();
            this.planValidatorClasses = new ArrayList<>();
            this.costEstimatorClasses = new ArrayList<>();
            this.planPruneStrategyClasses = new ArrayList<>();
            this.planSelectorClasses = new ArrayList<>();
        }
        //endregion

        //region Public Methods
        public Wrapper<P, C, CContext, Q> bind(Class<?> clazz) {
            this.bind(null, clazz);
            return this;
        }

        public Wrapper<P, C, CContext, Q> bind(String name, Class<?> clazz) {
            if (PlanSearcher.class.isAssignableFrom(clazz)) {
                this.planSearcherClasses.add(new AbstractMap.SimpleEntry<>(name, (Class<? extends PlanSearcher<P, C, Q>>)clazz));
            } else if (PlanExtensionStrategy.class.isAssignableFrom(clazz)) {
                this.planExtensionStrategyClasses.add(new AbstractMap.SimpleEntry<>(name, (Class<? extends PlanExtensionStrategy<P, Q>>)clazz));
            } else if (PlanValidator.class.isAssignableFrom(clazz)) {
                this.planValidatorClasses.add(new AbstractMap.SimpleEntry<>(name, (Class<? extends PlanValidator<P, Q>>)clazz));
            } else if (CostEstimator.class.isAssignableFrom(clazz)) {
                this.costEstimatorClasses.add(new AbstractMap.SimpleEntry<>(name, (Class<? extends CostEstimator<P, C, CContext>>)clazz));
            } else if (PlanPruneStrategy.class.isAssignableFrom(clazz)) {
                this.planPruneStrategyClasses.add(new AbstractMap.SimpleEntry<>(name, (Class<? extends PlanPruneStrategy<PlanWithCost<P, C>>>)clazz));
            } else if (PlanSelector.class.isAssignableFrom(clazz)) {
                this.planSelectorClasses.add(new AbstractMap.SimpleEntry<>(name, (Class<? extends PlanSelector<PlanWithCost<P, C>, Q>>)clazz));
            }

            return this;
        }
        //endregion

        //region Fields
        private PlanTraceOptions planTraceOptions;

        private List<Map.Entry<String, Class<? extends PlanSearcher<P, C, Q>>>> planSearcherClasses;
        private List<Map.Entry<String, Class<? extends PlanExtensionStrategy<P, Q>>>> planExtensionStrategyClasses;
        private List<Map.Entry<String, Class<? extends PlanValidator<P, Q>>>> planValidatorClasses;
        private List<Map.Entry<String, Class<? extends CostEstimator<P, C, CContext>>>> costEstimatorClasses;
        private List<Map.Entry<String, Class<? extends PlanPruneStrategy<PlanWithCost<P, C>>>>> planPruneStrategyClasses;
        private List<Map.Entry<String, Class<? extends PlanSelector<PlanWithCost<P, C>, Q>>>> planSelectorClasses;
        //endregion
    }
    //endregion

    //region PlanNode
    public static class PlanNode<P, C> {
        //region Constructors
        public PlanNode() {
            this.children = new ArrayList<>();
        }

        public PlanNode(P plan) {
            this();
            this.plan = plan;
        }
        //endregion

        //region Properties
        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public P getPlan() {
            return plan;
        }

        public void setPlan(P plan) {
            this.plan = plan;
        }

        public C getCost() {
            return cost;
        }

        public void setCost(C cost) {
            this.cost = cost;
        }

        public ValidationResult getValidationResult() {
            return validationResult;
        }

        public void setValidationResult(ValidationResult validationResult) {
            this.validationResult = validationResult;
        }

        public String getPrunedBy() {
            return prunedBy;
        }

        public void setPrunedBy(String prunedBy) {
            this.prunedBy = prunedBy;
        }

        public String getSelectedBy() {
            return selectedBy;
        }

        public void setSelectedBy(String selectedBy) {
            this.selectedBy = selectedBy;
        }

        public List<PlanNode> getChildren() {
            return children;
        }

        public void setChildren(List<PlanNode> children) {
            this.children = children;
        }

        public String getValidatedBy() {
            return validatedBy;
        }

        public void setValidatedBy(String validatedBy) {
            this.validatedBy = validatedBy;
        }

        public String getChosenBy() {
            return chosenBy;
        }

        public void setChosenBy(String chosenBy) {
            this.chosenBy = chosenBy;
        }

        public String getEstimatedBy() {
            return estimatedBy;
        }

        public void setEstimatedBy(String estimatedBy) {
            this.estimatedBy = estimatedBy;
        }
        //endregion

        //region Fields
        private P plan;
        private C cost;
        private ValidationResult validationResult;
        private String estimatedBy;
        private String validatedBy;
        private String createdBy;
        private String prunedBy;
        private String selectedBy;
        private String chosenBy;
        private List<PlanNode> children;
        //endregion
    }
    //endregion

    //region Builder
    public static class Builder<P, C> {
        //region Constructors
        public Builder() {
            this.roots = new ArrayList<>();
            this.planNodes = new HashMap<>();
        }
        //endregion

        //region Public Methods
        public Builder<P, C> addRoot(P plan, String createdBy) {
            PlanNode<P, C> root = new PlanNode<>(plan);
            root.setCreatedBy(createdBy);

            this.roots.add(root);
            this.planNodes.put(plan, root);
            return this;
        }

        public Builder<P, C> addNewPlan(P parentPlan, P childPlan, String createdBy) {
            PlanNode<P, C> parentNode = this.planNodes.get(parentPlan);
            if (parentNode == null) {
                // maybe log error??
                return this;
            }

            PlanNode<P, C> childPlanNode = new PlanNode<>(childPlan);
            childPlanNode.setCreatedBy(createdBy);

            parentNode.getChildren().add(childPlanNode);
            this.planNodes.put(childPlan, childPlanNode);
            return this;
        }

        public Builder<P, C> withValidation(P plan, ValidationResult validationResult, String validatedBy) {
            PlanNode<P, C> planNode = this.planNodes.get(plan);
            if (planNode == null) {
                return this;
            }

            planNode.setValidationResult(validationResult);
            planNode.setValidatedBy(validatedBy);
            return this;
        }

        public Builder<P, C> withEstimation(P plan, C cost, String estimatedBy) {
            PlanNode<P, C> planNode = this.planNodes.get(plan);
            if (planNode == null) {
                return this;
            }

            planNode.setCost(cost);
            planNode.setEstimatedBy(estimatedBy);
            return this;
        }

        public Builder<P, C> withPruning(P plan, String prunedBy) {
            PlanNode<P, C> planNode = this.planNodes.get(plan);
            if (planNode == null) {
                return this;
            }

            planNode.setPrunedBy(prunedBy);
            return this;
        }

        public Builder<P, C> withSelection(P plan, String selectedBy) {
            PlanNode<P, C> planNode = this.planNodes.get(plan);
            if (planNode == null) {
                return this;
            }

            planNode.setSelectedBy(selectedBy);
            return this;
        }

        public Builder<P, C> withChoise(P plan, String chosenBy) {
            PlanNode<P, C> planNode = this.planNodes.get(plan);
            if (planNode == null) {
                return this;
            }

            planNode.setChosenBy(chosenBy);
            return this;
        }

        public Iterable<PlanNode<P, C>> build() {
            return this.roots;
        }
        //endregion

        //region Fields
        private List<PlanNode<P, C>> roots;
        private Map<P, PlanNode<P, C>> planNodes;
        //endregion
    }
    //endregion

    //region Searcher
    public static class Searcher<P, C, Q> implements PlanSearcher<P, C, Q> {
        public static final String planSearcherParameter = "PlanTracer.Searcher.@planSearcher";
        public static final String planSearcherNameParameter = "PlanTracer.Searcher.@planSearcherName";

        //region Constructors
        @Inject
        public Searcher(
                @Named(planSearcherParameter) PlanSearcher<P, C, Q> planSearcher,
                @Named(planSearcherNameParameter) String planSearcherName,
                PlanTracer.Builder<P, C> builder,
                PlanTraceOptions planTraceOptions) {
            this.planSearcher = planSearcher;
            this.planSearcherName = planSearcherName;
            this.builder = builder;
            this.planTraceOptions = planTraceOptions;
        }
        //endregion

        //region PlanSearcher Implementation
        @Override
        public PlanWithCost<P, C> search(Q query) {
            PlanWithCost<P, C> planWithCost = this.planSearcher.search(query);
            if(planWithCost==null) throw new NoPlanFoundError("No Plan found for query["+query.toString()+"]");

            this.builder.withChoise(planWithCost.getPlan(), this.planSearcherName);
            return planWithCost;
        }
        //endregion

        //region Properties
        public Builder<P, C> getBuilder() {
            return this.builder;
        }
        //endregion

        //region Fields
        private PlanSearcher<P, C, Q> planSearcher;
        private String planSearcherName;
        private PlanTracer.Builder<P, C> builder;
        private PlanTraceOptions planTraceOptions;
        //endregion

        //region Provider
        public static class Provider<P, C, Q> implements com.google.inject.Provider<PlanSearcher<P, C, Q>> {
            public static final String planSearcherParameter = "PlanTracer.Searcher.Provider.@planSearcher";
            public static final String planSearcherNameParameter = "PlanTracer.Searcher.Provider.@planSearcherName";

            //region Constructors
            @Inject
            public Provider(
                    @Named(planSearcherParameter) PlanSearcher<P, C, Q> planSearcher,
                    @Named(planSearcherNameParameter) String planSearcherName,
                    PlanTracer.Builder<P, C> builder,
                    PlanTraceOptions planTraceOptions) {
                this.planSearcher = planSearcher;
                this.planSearcherName = planSearcherName;
                this.builder = builder;
                this.planTraceOptions = planTraceOptions;
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanSearcher<P, C, Q> get() {
                return this.planTraceOptions.getLevel() == PlanTraceOptions.Level.none ?
                        this.planSearcher :
                        new Searcher<>(this.planSearcher, this.planSearcherName, this.builder, this.planTraceOptions);
            }
            //endregion

            //region Fields
            private PlanSearcher<P, C, Q> planSearcher;
            private String planSearcherName;
            private PlanTracer.Builder<P, C> builder;
            private PlanTraceOptions planTraceOptions;
            //endregion
        }
        //endregion
    }
    //endregion

    //region ExtensionStrategy
    public static class ExtensionStrategy<P extends IPlan, C, Q extends IQuery> implements PlanExtensionStrategy<P, Q> {
        public static final String planExtensionStrategyParameter = "PlanTracer.ExtensionStrategy.@planExtensionStrategy";
        public static final String planExtensionStrategyNameParameter = "PlanTracer.ExtensionStrategy.@planExtensionStrategyName";

        //region Constructors
        @Inject
        public ExtensionStrategy(
                @Named(planExtensionStrategyParameter) PlanExtensionStrategy<P, Q> planExtensionStrategy,
                @Named(planExtensionStrategyNameParameter) String planExtensionStrategyName,
                PlanTracer.Builder<P, C> builder,
                PlanTraceOptions planTraceOptions) {
            this.planExtensionStrategy = planExtensionStrategy;
            this.planExtensionStrategyName = planExtensionStrategyName;
            this.builder = builder;
            this.planTraceOptions = planTraceOptions;
        }
        //endregion

        //region PlanExtensionStrategy Implementation
        @Override
        public Iterable<P> extendPlan(Optional<P> plan, Q query) {
            Iterable<P> childPlans = this.planExtensionStrategy.extendPlan(plan, query);
            if (plan.isPresent()) {
                Stream.ofAll(childPlans).forEach(childPlan -> this.builder.addNewPlan(plan.get(), childPlan, this.planExtensionStrategyName));
            } else {
                Stream.ofAll(childPlans).forEach(childPlan -> this.builder.addRoot(childPlan, this.planExtensionStrategyName));
            }

            return childPlans;
        }
        //endregion

        //region Properties
        public Builder<P, C> getBuilder() {
            return this.builder;
        }
        //endregion

        //region Fields
        private PlanExtensionStrategy<P, Q> planExtensionStrategy;
        private String planExtensionStrategyName;
        private PlanTracer.Builder<P, C> builder;
        private PlanTraceOptions planTraceOptions;
        //endregion

        //region Provider
        public static class Provider<P extends IPlan, C, Q extends IQuery> implements com.google.inject.Provider<PlanExtensionStrategy<P, Q>> {
            public static final String planExtensionStrategyParameter = "PlanTracer.ExtensionStrategy.Provider.@planExtensionStrategy";
            public static final String planExtensionStrategyNameParameter = "PlanTracer.ExtensionStrategy.Provider.@planExtensionStrategyName";

            //region Constructors
            @Inject
            public Provider(
                    @Named(planExtensionStrategyParameter) PlanExtensionStrategy<P, Q> planExtensionStrategy,
                    @Named(planExtensionStrategyNameParameter) String planExtensionStrategyName,
                    PlanTracer.Builder<P, C> builder,
                    PlanTraceOptions planTraceOptions) {
                this.planExtensionStrategy = planExtensionStrategy;
                this.planExtensionStrategyName = planExtensionStrategyName;
                this.builder = builder;
                this.planTraceOptions = planTraceOptions;
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanExtensionStrategy<P, Q> get() {
                return this.planTraceOptions.getLevel() == PlanTraceOptions.Level.none ?
                        this.planExtensionStrategy :
                        new ExtensionStrategy<>(this.planExtensionStrategy, this.planExtensionStrategyName, this.builder, this.planTraceOptions);

            }
            //endregion

            //region Fields
            private PlanExtensionStrategy<P, Q> planExtensionStrategy;
            private String planExtensionStrategyName;
            private PlanTracer.Builder<P, C> builder;
            private PlanTraceOptions planTraceOptions;
            //endregion
        }
        //endregion
    }
    //endregion

    //region Validator
    public static class Validator<P, C, Q> implements PlanValidator<P, Q> {
        public static final String planValidatorParameter = "PlanTracer.Validator.@planValidator";
        public static final String planValidatorNameParameter = "PlanTracer.Validator.@planValidatorName";

        //region Constructors
        @Inject
        public Validator(
                @Named(planValidatorParameter) PlanValidator<P, Q> planValidator,
                @Named(planValidatorNameParameter) String planValidatorName,
                PlanTracer.Builder<P, C> builder,
                PlanTraceOptions planTraceOptions) {
            this.planValidator = planValidator;
            this.planValidatorName = planValidatorName;
            this.builder = builder;
            this.planTraceOptions = planTraceOptions;
        }
        //endregion

        //region PlanValidator Implementation
        @Override
        public ValidationResult isPlanValid(P plan, Q query) {
            ValidationResult validationResult = this.planValidator.isPlanValid(plan, query);
            this.builder.withValidation(plan, validationResult, this.planValidatorName);
            return validationResult;
        }
        //endregion

        //region Properties
        public Builder<P, C> getBuilder() {
            return this.builder;
        }
        //endregion

        //region Fields
        private PlanValidator<P, Q> planValidator;
        private String planValidatorName;
        private PlanTracer.Builder<P, C> builder;
        private PlanTraceOptions planTraceOptions;
        //endregion

        //region Provider
        public static class Provider<P, C, Q> implements com.google.inject.Provider<PlanValidator<P, Q>> {
            public static final String planValidatorParameter = "PlanTracer.Validator.Provider.@planValidator";
            public static final String planValidatorNameParameter = "PlanTracer.Validator.Provider.@planValidatorName";

            //region Constructors
            @Inject
            public Provider(
                    @Named(planValidatorParameter) PlanValidator<P, Q> planValidator,
                    @Named(planValidatorNameParameter) String planValidatorName,
                    PlanTracer.Builder<P, C> builder,
                    PlanTraceOptions planTraceOptions) {
                this.planValidator = planValidator;
                this.planValidatorName = planValidatorName;
                this.builder = builder;
                this.planTraceOptions = planTraceOptions;
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanValidator<P, Q> get() {
                return this.planTraceOptions.getLevel() == PlanTraceOptions.Level.none ?
                        this.planValidator :
                        new Validator<>(this.planValidator, this.planValidatorName, this.builder, this.planTraceOptions);

            }
            //endregion

            //region Fields
            private PlanValidator<P, Q> planValidator;
            private String planValidatorName;
            private PlanTracer.Builder<P, C> builder;
            private PlanTraceOptions planTraceOptions;
            //endregion
        }
        //endregion
    }
    //endregion

    //region Estimator
    public static class Estimator<P, C, TContext> implements CostEstimator<P, C, TContext> {
        public static final String costEstimatorParameter = "PlanTracer.Estimator.@costEstimator";
        public static final String costEstimatorNameParameter = "PlanTracer.Estimator.@costEstimatorName";

        //region Constructors
        @Inject
        public Estimator(
                @Named(costEstimatorParameter) CostEstimator<P, C, TContext> costEstimator,
                @Named(costEstimatorNameParameter) String costEstimatorName,
                PlanTracer.Builder<P, C> builder,
                PlanTraceOptions planTraceOptions) {
            this.costEstimator = costEstimator;
            this.costEstimatorName = costEstimatorName;
            this.builder = builder;
            this.planTraceOptions = planTraceOptions;
        }
        //endregion

        //region CostEstimator Implementation
        @Override
        public PlanWithCost<P, C> estimate(P plan, TContext context) {
            PlanWithCost<P, C> planWithCost = this.costEstimator.estimate(plan, context);
            this.builder.withEstimation(plan, planWithCost.getCost(), this.costEstimatorName);
            return planWithCost;
        }
        //endregion

        //region Properties
        public Builder<P, C> getBuilder() {
            return this.builder;
        }
        //endregion

        //region Fields
        private CostEstimator<P, C, TContext> costEstimator;
        private String costEstimatorName;
        private PlanTracer.Builder<P, C> builder;
        private PlanTraceOptions planTraceOptions;
        //endregion

        //region Provider
        public static class Provider<P, C, TContext> implements com.google.inject.Provider<CostEstimator<P, C, TContext>> {
            public static final String costEstimatorParameter = "PlanTracer.Estimator.Provider.@costEstimator";
            public static final String costEstimatorNameParameter = "PlanTracer.Estimator.Provider.@costEstimatorName";

            //region Constructors
            @Inject
            public Provider(
                    @Named(costEstimatorParameter) CostEstimator<P, C, TContext> costEstimator,
                    @Named(costEstimatorNameParameter) String costEstimatorName,
                    PlanTracer.Builder<P, C> builder,
                    PlanTraceOptions planTraceOptions) {
                this.costEstimator = costEstimator;
                this.costEstimatorName = costEstimatorName;
                this.builder = builder;
                this.planTraceOptions = planTraceOptions;
            }
            //endregion

            //region Provider Implementation
            @Override
            public CostEstimator<P, C, TContext> get() {
                return this.planTraceOptions.getLevel() == PlanTraceOptions.Level.none ?
                        this.costEstimator :
                        new Estimator<>(this.costEstimator, this.costEstimatorName, this.builder, this.planTraceOptions);

            }
            //endregion

            //region Fields
            private CostEstimator<P, C, TContext> costEstimator;
            private String costEstimatorName;
            private PlanTracer.Builder<P, C> builder;
            private PlanTraceOptions planTraceOptions;
            //endregion
        }
        //endregion
    }
    //endregion

    //region PruneStrategy
    public static class PruneStrategy<P, C> implements PlanPruneStrategy<PlanWithCost<P, C>> {
        public static final String planPruneStrategyParameter = "PlanTracer.PruneStrategy.@planPruneStrategy";
        public static final String planPruneStrategyNameParameter = "PlanTracer.PruneStrategy.@planPruneStrategyName";

        //region Constructors
        @Inject
        public PruneStrategy(
                @Named(planPruneStrategyParameter) PlanPruneStrategy<PlanWithCost<P, C>> planPruneStrategy,
                @Named(planPruneStrategyNameParameter) String planPruneStrategyName,
                PlanTracer.Builder<P, C> builder,
                PlanTraceOptions planTraceOptions) {
            this.planPruneStrategy = planPruneStrategy;
            this.planPruneStrategyName = planPruneStrategyName;
            this.builder = builder;
            this.planTraceOptions = planTraceOptions;
        }
        //endregion

        //region PlanPruneStrategy Implementation
        @Override
        public Iterable<PlanWithCost<P, C>> prunePlans(Iterable<PlanWithCost<P, C>> plans) {
            Iterable<PlanWithCost<P, C>> prunedPlans = this.planPruneStrategy.prunePlans(plans);
            Set<PlanWithCost<P, C>> prunedPlansSet = Stream.ofAll(prunedPlans).toJavaSet();
            Stream.ofAll(plans)
                    .filter(planWithCost -> !prunedPlansSet.contains(planWithCost))
                    .forEach(planWithCost -> this.builder.withPruning(planWithCost.getPlan(), this.planPruneStrategyName));

            return prunedPlans;
        }
        //endregion

        //region Properties
        public Builder<P, C> getBuilder() {
            return this.builder;
        }
        //endregion

        //region Fields
        private PlanPruneStrategy<PlanWithCost<P, C>> planPruneStrategy;
        private String planPruneStrategyName;
        private PlanTracer.Builder<P, C> builder;
        private PlanTraceOptions planTraceOptions;
        //endregion

        //region Provider
        public static class Provider<P, C> implements com.google.inject.Provider<PlanPruneStrategy<PlanWithCost<P, C>>> {
            public static final String planPruneStrategyParameter = "PlanTracer.PruneStrategy.Provider.@planPruneStrategy";
            public static final String planPruneStrategyNameParameter = "PlanTracer.PruneStrategy.Provider.@planPruneStrategyName";

            //region Constructors
            @Inject
            public Provider(
                    @Named(planPruneStrategyParameter) PlanPruneStrategy<PlanWithCost<P, C>> planPruneStrategy,
                    @Named(planPruneStrategyNameParameter) String planPruneStrategyName,
                    PlanTracer.Builder<P, C> builder,
                    PlanTraceOptions planTraceOptions) {
                this.planPruneStrategy = planPruneStrategy;
                this.planPruneStrategyName = planPruneStrategyName;
                this.builder = builder;
                this.planTraceOptions = planTraceOptions;
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanPruneStrategy<PlanWithCost<P, C>> get() {
                return this.planTraceOptions.getLevel() == PlanTraceOptions.Level.none ?
                        this.planPruneStrategy :
                        new PruneStrategy<>(this.planPruneStrategy, this.planPruneStrategyName, this.builder, this.planTraceOptions);
            }
            //endregion

            //region Fields
            private PlanPruneStrategy<PlanWithCost<P, C>> planPruneStrategy;
            private String planPruneStrategyName;
            private PlanTracer.Builder<P, C> builder;
            private PlanTraceOptions planTraceOptions;
            //endregion
        }
        //endregion
    }
    //endregion

    //region Selector
    public static class Selector<P, C, Q> implements PlanSelector<PlanWithCost<P, C>, Q> {
        public static final String planSelectorParameter = "PlanTracer.Selector.@planSelector";
        public static final String planSelectorNameParameter = "PlanTracer.Selector.@planSelectorName";

        //region Constructors
        @Inject
        public Selector(
                @Named(planSelectorParameter) PlanSelector<PlanWithCost<P, C>, Q> planSelector,
                @Named(planSelectorNameParameter) String planSelectorName,
                PlanTracer.Builder<P, C> builder,
                PlanTraceOptions planTraceOptions) {
            this.planSelector = planSelector;
            this.planSelectorName = planSelectorName;
            this.builder = builder;
            this.planTraceOptions = planTraceOptions;
        }
        //endregion

        //region PlanSelector Implementation
        @Override
        public Iterable<PlanWithCost<P, C>> select(Q query, Iterable<PlanWithCost<P, C>> plans) {
            Iterable<PlanWithCost<P, C>> selectedPlans = this.planSelector.select(query, plans);
            Stream.ofAll(selectedPlans).forEach(planWithCost -> this.builder.withSelection(planWithCost.getPlan(), this.planSelectorName));
            return selectedPlans;
        }
        //endregion

        //region Properties
        public Builder<P, C> getBuilder() {
            return this.builder;
        }
        //endregion

        //region Fields
        private PlanSelector<PlanWithCost<P, C>, Q> planSelector;
        private String planSelectorName;
        private PlanTracer.Builder<P, C> builder;
        private PlanTraceOptions planTraceOptions;
        //endregion

        //region Provider
        public static class Provider<P, C, Q> implements com.google.inject.Provider<PlanSelector<PlanWithCost<P, C>, Q>> {
            public static final String planSelectorParameter = "PlanTracer.Selector.Provider.@planSelector";
            public static final String planSelectorNameParameter = "PlanTracer.Selector.Provider.@planSelectorName";

            //region Constructors
            @Inject
            public Provider(
                    @Named(planSelectorParameter) PlanSelector<PlanWithCost<P, C>, Q> planSelector,
                    @Named(planSelectorNameParameter) String planSelectorName,
                    PlanTracer.Builder<P, C> builder,
                    PlanTraceOptions planTraceOptions) {
                this.planSelector = planSelector;
                this.planSelectorName = planSelectorName;
                this.builder = builder;
                this.planTraceOptions = planTraceOptions;
            }
            //endregion

            //region Provider Implementation
            @Override
            public PlanSelector<PlanWithCost<P, C>, Q> get() {
                return this.planTraceOptions.getLevel() == PlanTraceOptions.Level.none ?
                        this.planSelector :
                        new Selector<>(this.planSelector, this.planSelectorName, this.builder, this.planTraceOptions);
            }
            //endregion

            //region Fields
            private PlanSelector<PlanWithCost<P, C>, Q> planSelector;
            private String planSelectorName;
            private PlanTracer.Builder<P, C> builder;
            private PlanTraceOptions planTraceOptions;
            //endregion
        }
        //endregion
    }
    //endregion
}
