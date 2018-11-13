package com.kayhut.fuse.epb.plan.pruners;

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

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.model.execution.plan.IPlan;

/**
 * Created by moti on 2/23/2017.
 */
public class NoPruningPruneStrategy<P extends IPlan> implements PlanPruneStrategy<P> {
    @Override
    public Iterable<P> prunePlans(Iterable<P> plans) {
        return plans;
    }
}
