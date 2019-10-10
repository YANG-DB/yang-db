package com.yangdb.fuse.model.execution.plan.composite.descriptors;

/*-
 *
 * RelationOpDescriptor.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;

/**
 * Created by Roman on 3/13/2018.
 */
public class RelationOpDescriptor implements Descriptor<RelationOp> {
    //region Descriptor Implementation
    @Override
    public String describe(RelationOp item) {
        return String.format("%s(%s(%s))",
                item.getClass().getSimpleName(),
                item.getAsgEbase().geteBase().getrType(),
                item.getAsgEbase().geteBase().geteNum());
    }
    //endregion
}
