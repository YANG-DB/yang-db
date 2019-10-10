package com.yangdb.fuse.epb.plan.estimation.pattern.estimators;

/*-
 *
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.epb.plan.estimation.pattern.Pattern;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class CompositePatternCostEstimator<P, C, TContext> implements PatternCostEstimator<P, C, TContext> {
    //region Constructors
    public CompositePatternCostEstimator(Map<Class<? extends Pattern>, PatternCostEstimator<P, C, TContext>> estimators) {
        this.estimators = new HashMap<>(estimators);
    }
    //endregion

    //region PatternCostEstimator Implementation
    @Override
    public Result<P, C> estimate(Pattern pattern, TContext context) {
        PatternCostEstimator<P, C, TContext> estimator = this.estimators.get(pattern.getClass());
        if (estimator == null) {
            return PatternCostEstimator.EmptyResult.get();
        }

        return estimator.estimate(pattern, context);
    }
    //endregion


    public Map<Class<? extends Pattern>, PatternCostEstimator<P, C, TContext>> getEstimators() {
        return estimators;
    }

    //region Fields
    private Map<Class<? extends Pattern>, PatternCostEstimator<P, C, TContext>> estimators;
    //endregion
}
