package com.kayhut.fuse.epb.plan.estimation.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;

/**
 * Created by roman.margolis on 15/03/2018.
 */
public class CachedCostEstimator<P, C, TContext> implements CostEstimator<P, C, TContext> {
    public static final String costEstimatorParameter = "CachedCostEstimator.@costEstimator";
    public static final String cacheParameter = "CachedCostEstimator.@cache";
    public static final String descriptorParameter = "CachedCostEstimator.@descriptor";

    //region Constructors
    @Inject
    public CachedCostEstimator(
            @Named(costEstimatorParameter) CostEstimator<P, C, TContext> costEstimator,
            @Named(cacheParameter) Cache<String, C> cache,
            @Named(descriptorParameter) Descriptor<P> descriptor) {
        this.costEstimator = costEstimator;
        this.cache = cache;
        this.descriptor = descriptor;
    }
    //endregion

    //region CostEstimator Implementation
    @Override
    public PlanWithCost<P, C> estimate(P plan, TContext context) {
        return new PlanWithCost<>(plan,
                this.cache.get(
                    this.descriptor.describe(plan),
                    key -> this.costEstimator.estimate(plan, context).getCost()));
    }
    //endregion

    //region Fields
    private CostEstimator<P, C, TContext> costEstimator;
    private Cache<String, C> cache;
    private Descriptor<P> descriptor;
    //endregion
}
