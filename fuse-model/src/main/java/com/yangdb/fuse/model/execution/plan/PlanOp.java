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
 * PlanOp.java - fuse-model - yangdb - 2,016
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
import com.yangdb.fuse.model.execution.plan.composite.CompositeAsgEBasePlanOp;
import com.yangdb.fuse.model.execution.plan.composite.CompositePlanOp;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.composite.UnionOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityGroupByOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationGroupingOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;

/**
 * Created by lior.perry on 20/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
//todo - populate sub hierarchy jackson
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "Plan", value = Plan.class),
        @JsonSubTypes.Type(name = "AsgEBasePlanOp", value = AsgEBasePlanOp.class),
        @JsonSubTypes.Type(name = "AttachedPropertyFilterOp", value = AttachedPropertyFilterOp.class),
        @JsonSubTypes.Type(name = "EntityFilterOp", value = EntityFilterOp.class),
        @JsonSubTypes.Type(name = "RelationFilterOp", value = RelationFilterOp.class),
        @JsonSubTypes.Type(name = "FilterOp", value = FilterOp.class),
        @JsonSubTypes.Type(name = "CompositeFilterOp", value = CompositeAsgEBasePlanOp.class),
        @JsonSubTypes.Type(name = "CompositePlanOp", value = CompositePlanOp.class),
        @JsonSubTypes.Type(name = "CompositeAsgEBasePlanOp", value = CompositeAsgEBasePlanOp.class),
        @JsonSubTypes.Type(name = "RelationOp", value = RelationOp.class),
        @JsonSubTypes.Type(name = "EntityOp", value = EntityOp.class),
        @JsonSubTypes.Type(name = "EntityGroupByOp", value = EntityGroupByOp.class),
        @JsonSubTypes.Type(name = "RelationGroupingOp", value = RelationGroupingOp.class),
        @JsonSubTypes.Type(name = "UnionOp", value = UnionOp.class),
})
public abstract class PlanOp {
}
