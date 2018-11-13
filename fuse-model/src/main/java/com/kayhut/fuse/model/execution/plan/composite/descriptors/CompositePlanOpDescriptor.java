package com.kayhut.fuse.model.execution.plan.composite.descriptors;

/*-
 * #%L
 * CompositePlanOpDescriptor.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import com.google.inject.Inject;
import com.kayhut.fuse.model.descriptors.CompositeDescriptor;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.descriptors.ToStringDescriptor;
import com.kayhut.fuse.model.execution.plan.AsgEBaseContainer;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositeAsgEBasePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class CompositePlanOpDescriptor implements Descriptor<CompositePlanOp> {
    //region Constructors
    @Inject
    public CompositePlanOpDescriptor(Descriptor<Iterable<PlanOp>> planOpsDescriptor) {
        this.planOpsDescriptor = planOpsDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(CompositePlanOp compositePlanOp) {
        return new StringBuilder()
                .append(compositePlanOp.getClass().getSimpleName())
                .append("[")
                .append(this.planOpsDescriptor.describe(compositePlanOp.getOps()))
                .append("]")
                .toString();
    }
    //endregion

    //region Fields
    private Descriptor<Iterable<PlanOp>> planOpsDescriptor;
    //endregion
}
