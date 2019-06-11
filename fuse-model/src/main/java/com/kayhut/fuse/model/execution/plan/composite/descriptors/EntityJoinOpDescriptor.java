package com.kayhut.fuse.model.execution.plan.composite.descriptors;

/*-
 * #%L
 * EntityJoinOpDescriptor.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;

public class EntityJoinOpDescriptor implements Descriptor<EntityJoinOp> {
    public EntityJoinOpDescriptor(Descriptor<Iterable<PlanOp>> planOpsDescriptor) {
        this.planOpsDescriptor = planOpsDescriptor;
    }

    @Override
    public String describe(EntityJoinOp item) {
        return new StringBuilder()
                .append(item.getClass().getSimpleName())
                .append("(")
                .append(item)
                .append(")")
                .append("[left:[")
                .append(this.planOpsDescriptor.describe(item.getLeftBranch().getOps()))
                .append("], right:[")
                .append(this.planOpsDescriptor.describe(item.getRightBranch().getOps()))
                .append("]]")
                .toString();
    }

    private Descriptor<Iterable<PlanOp>> planOpsDescriptor;
}
