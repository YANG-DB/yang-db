package com.yangdb.fuse.epb.plan.pruners;

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

import com.yangdb.fuse.dispatcher.epb.PlanPruneStrategy;
import com.yangdb.fuse.model.execution.plan.IPlan;
import javaslang.collection.Stream;

import java.util.*;

public class CompositePruner<P extends IPlan> implements PlanPruneStrategy<P> {
    public CompositePruner(List<PlanPruneStrategy<P>> planPruners) {
        this.planPruners = planPruners;
    }

    @Override
    public Iterable<P> prunePlans(Iterable<P> plans) {
        List<Set<P>> prunedPlansList = new ArrayList<>();
        for (PlanPruneStrategy<P> planPruner : this.planPruners) {
            prunedPlansList.add(Stream.ofAll(planPruner.prunePlans(plans)).toJavaSet());
        }

        if(prunedPlansList.size() > 0){
            Set<P> plansIntersection = new HashSet<>(prunedPlansList.get(0));

            for(int i = 1;i<prunedPlansList.size();i++){
                plansIntersection.retainAll(prunedPlansList.get(i));
            }
            return plansIntersection;
        }
        return Collections.emptyList();
    }
    private List<PlanPruneStrategy<P>> planPruners;
}
