package com.kayhut.fuse.dispatcher.driver;

/*-
 * #%L
 * fuse-core
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

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.CreateJsonQueryRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.ExecuteStoredQueryRequest;

import java.util.Optional;

/**
 * Created by lior.perry on 21/02/2017.
 */
public interface QueryDriver {

    Optional<QueryResourceInfo> create(CreateQueryRequest queryRequest);

    Optional<QueryResourceInfo> create(CreateJsonQueryRequest queryRequest);

    Optional<QueryResourceInfo> call(ExecuteStoredQueryRequest queryRequest);

    Optional<QueryResourceInfo> createAndFetch(CreateQueryRequest queryRequest);

    Optional<Object> getNextPageData(String queryId, Optional<String> cursorId,int pageSize, boolean deleteCurrentPage);

    Optional<StoreResourceInfo> getInfo();

    Optional<QueryResourceInfo> getInfo(String queryId);

    Optional<Query> getV1(String queryId);

    Optional<AsgQuery> getAsg(String queryId);

    Optional<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId);

    Optional<PlanNode<Plan>> planVerbose(String queryId);

    Optional<Boolean> delete(String queryId);
}
