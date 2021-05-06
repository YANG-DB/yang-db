package com.yangdb.fuse.model.execution.plan;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

/*-
 *
 * AsgEBasePlanOp.java - fuse-model - yangdb - 2,016
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.query.EBase;

/**
 * Created by Roman on 30/04/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsgEBasePlanOp<T extends EBase> extends PlanOp implements AsgEBaseContainer<T> {
    //region Constructors
    public AsgEBasePlanOp(AsgEBase<T> asgEBase) {
        this.asgEbase = asgEBase;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.asgEbase.toString() + ")";
    }
    //endregion

    //region Properties
    @Override
    public AsgEBase<T> getAsgEbase() {
        return asgEbase;
    }

    public void setAsgEbase(AsgEBase<T> value) {
        this.asgEbase = value;
    }
    //endregion

    //region Fields
    private AsgEBase<T> asgEbase;
    //endregion
}
