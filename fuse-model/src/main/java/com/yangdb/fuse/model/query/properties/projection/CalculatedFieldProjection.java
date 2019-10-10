package com.yangdb.fuse.model.query.properties.projection;

/*-
 *
 * IdentityProjection.java - fuse-model - yangdb - 2,016
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

import com.yangdb.fuse.model.query.aggregation.AggLOp;

public class CalculatedFieldProjection extends Projection {
    private AggLOp expression;

    public CalculatedFieldProjection() {}

    public CalculatedFieldProjection(AggLOp expression) {
        this.expression = expression;
    }

    public void setExpression(AggLOp expression) {
        this.expression = expression;
    }

    public AggLOp getExpression() {
        return expression;
    }
}
