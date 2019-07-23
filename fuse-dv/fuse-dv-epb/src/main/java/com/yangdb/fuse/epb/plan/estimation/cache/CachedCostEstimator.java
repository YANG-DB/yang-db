package com.yangdb.fuse.epb.plan.estimation.cache;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.epb.CostEstimator;
import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;

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
