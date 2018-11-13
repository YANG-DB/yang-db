package com.kayhut.fuse.epb.plan.estimation.cache;

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

import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;

/**
 * Created by roman.margolis on 15/03/2018.
 */
public class EntityOpDescriptor implements Descriptor<EntityOp> {
    @Override
    public String describe(EntityOp item) {
        return ETyped.class.isAssignableFrom(item.getAsgEbase().geteBase().getClass()) ?
                EConcrete.class.isAssignableFrom(item.getAsgEbase().geteBase().getClass()) ?
                        ((EConcrete)item.getAsgEbase().geteBase()).geteType() + "(" + ((EConcrete)item.getAsgEbase().geteBase()).geteID() + ")" :
                        ((ETyped)item.getAsgEbase().geteBase()).geteType() :
                EUntyped.class.getSimpleName();
    }
}
