package com.kayhut.fuse.gta.strategy.promise;

/*-
 * #%L
 * fuse-dv-gta
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

import com.kayhut.fuse.gta.strategy.common.CompositePlanOpTranslationStrategy;
import com.kayhut.fuse.gta.strategy.common.EntityTranslationOptions;
import com.kayhut.fuse.gta.strategy.common.GoToEntityOpTranslationStrategy;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;

/**
 * Created by Roman on 14/05/2017.
 */
public class M1FilterPlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M1FilterPlanOpTranslationStrategy() {
        super(
                new EntityOpTranslationStrategy(EntityTranslationOptions.filterEntity),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.filterEntity),
                        new EntitySelectionTranslationStrategy()),
                new RelationFilterOpTranslationStrategy());
    }
    //endregion
}

