package com.kayhut.fuse.model.execution.plan.entity;

/*-
 * #%L
 * EntityGroupByOp.java - fuse-model - kayhut - 2,016
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
import com.kayhut.fuse.model.query.aggregation.AggBase;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class EntityGroupByOp extends AsgEBasePlanOp<AggBase> {
    //region Constructor
    public EntityGroupByOp() {
        super(new AsgEBase<>());
    }

    public EntityGroupByOp(String name, String vertexTag, AsgEBase<AggBase> agg) {
        super(agg);
        this.name = name;
        this.vertexTag = vertexTag;
    }
    //endregion

    //region Properties
    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getVertexTag() {
        return this.vertexTag;
    }

    public void setVertexTag(String value) {
        this.vertexTag = value;
    }

    //endregion

    //region Fields
    private String vertexTag;
    private String name;
    //endregion
}
