package com.kayhut.fuse.epb.plan.estimation.pattern;

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

import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;

import java.util.Optional;

/**
 * Created by Roman on 29/06/2017.
 */
public class EntityPattern extends Pattern {
    //region Constructors
    public EntityPattern(EntityOp start, EntityFilterOp startFilter) {
        this.start = start;
        this.startFilter = startFilter;
    }
    //endregion

    //region Properties
    public EntityOp getStart() {
        return start;
    }

    public EntityFilterOp getStartFilter() {
        return startFilter;
    }
    //endregion

    //region Fields
    private EntityOp start;
    private EntityFilterOp startFilter;

    //endregion
}
