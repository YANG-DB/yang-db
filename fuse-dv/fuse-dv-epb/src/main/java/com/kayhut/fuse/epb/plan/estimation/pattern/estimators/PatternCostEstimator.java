package com.kayhut.fuse.epb.plan.estimation.pattern.estimators;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.epb.plan.estimation.pattern.Pattern;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;

import java.util.*;

/**
 * Created by lior.perry on 4/24/2017.
 */
public interface PatternCostEstimator<P1, C1, TContext> {
    interface EmptyResult<P3, C3> extends Result<P3, C3> {
        static <P2, C2> Result<P2, C2> get() {
            return new EmptyResult<P2, C2>() {
                @Override
                public List<PlanWithCost<P2, C2>> getPlanStepCosts() {
                    return Collections.emptyList();
                }

                @Override
                public double[] countsUpdateFactors() {
                    return new double[] { 0 };
                }
            };
        }
    }

    interface Result<P2, C2> {
        List<PlanWithCost<P2, C2>> getPlanStepCosts();

        double[] countsUpdateFactors();

        @SafeVarargs
        static <P2, C2> Result<P2, C2> of(double[] countsUpdateFactors, PlanWithCost<P2, C2> ... planStepCosts) {
            return new Result<P2, C2>() {
                @Override
                public List<PlanWithCost<P2, C2>> getPlanStepCosts() {
                    return Arrays.asList(planStepCosts);
                }

                @Override
                public double[] countsUpdateFactors() {
                    return countsUpdateFactors;
                }
            };
        }
    }


    Result<P1, C1> estimate(Pattern pattern, TContext context);
}
