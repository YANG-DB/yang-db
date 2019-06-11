package com.kayhut.fuse.model.execution.plan.composite;

/*-
 * #%L
 * CompositeFilterOp.java - fuse-model - kayhut - 2,016
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

import com.kayhut.fuse.model.execution.plan.FilterOp;
import com.kayhut.fuse.model.execution.plan.LogicalOperator;

import java.util.List;

/**
 * Created by lior.perry on 23/02/2017.
 */
public class CompositeFilterOp extends FilterOp {
    //region Constructors
    public CompositeFilterOp() {

    }

    public CompositeFilterOp(LogicalOperator op, List<FilterOp> filters) {
        this.op = op;
        this.filters = filters;
    }
    //endregion

    //region Properties
    public LogicalOperator getOp() {
        return this.op;
    }

    public void setOp(LogicalOperator op) {
        this.op = op;
    }

    public List<FilterOp> getFilters() {
        return this.filters;
    }

    public void setFilters(List<FilterOp> value) {
        this.filters = value;
    }
    //endregion

    //region Fields
    private LogicalOperator op;
    private List<FilterOp> filters;
    //endregion
}
