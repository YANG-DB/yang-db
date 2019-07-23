package com.yangdb.fuse.epb.plan.selectors;

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

import com.yangdb.fuse.dispatcher.epb.PlanSelector;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;

/**
 * Created by benishue on 11-May-17.
 */
public class SelectAllPlanSelector<C> implements PlanSelector<PlanWithCost<Plan, C>, AsgQuery> {
    //region PlanSelector Implementation
    @Override
    public Iterable<PlanWithCost<Plan, C>> select(AsgQuery query, Iterable<PlanWithCost<Plan, C>> plans) {
        return plans;
    }
    //endregion
}
