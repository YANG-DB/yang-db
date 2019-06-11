package com.kayhut.fuse.model.execution.plan.composite;

/*-
 * #%L
 * OptionalOp.java - fuse-model - kayhut - 2,016
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

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import javaslang.collection.Stream;

/**
 * Created by lior.perry on 23/02/2017.
 */
public class OptionalOp extends CompositeAsgEBasePlanOp<OptionalComp> {
    //region Constructors

    public OptionalOp() {}

    public OptionalOp(AsgEBase<OptionalComp> asgEBase, Iterable<PlanOp> ops) {
        super(asgEBase, ops);
    }

    public OptionalOp(AsgEBase<OptionalComp> asgEBase, PlanOp...ops) {
        this(asgEBase, Stream.of(ops));
    }
    //endregion
}
