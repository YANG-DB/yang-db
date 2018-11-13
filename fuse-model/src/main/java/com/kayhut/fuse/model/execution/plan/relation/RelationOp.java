package com.kayhut.fuse.model.execution.plan.relation;

/*-
 * #%L
 * RelationOp.java - fuse-model - kayhut - 2,016
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

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.AsgEBasePlanOp;
import com.kayhut.fuse.model.query.Rel;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class RelationOp extends AsgEBasePlanOp<Rel> {
    //region Constructors
    public RelationOp() {
        super(new AsgEBase<>());
    }

    public RelationOp(AsgEBase<Rel> relation) {
        super(relation);
    }

    public RelationOp(AsgEBase<Rel> relation, Rel.Direction direction) {
        super(new AsgEBase<>(relation.geteBase().clone()));
        getAsgEbase().geteBase().setDir(direction);
    }
    //endregion
}
