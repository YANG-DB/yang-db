package com.yangdb.fuse.dispatcher.driver;

/*-
 * #%L
 * fuse-core
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



import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.StoreResourceInfo;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by lior.perry on 21/02/2017.
 */
public interface CursorDriver {
    Optional<CursorResourceInfo> create(String queryId, CreateCursorRequest cursorRequest);
    Optional<StoreResourceInfo> getInfo(String queryId);
    Optional<CursorResourceInfo> getInfo(String queryId, String cursorId);
    Optional<GraphTraversal> traversal(PlanWithCost plan, String ontology);
    Optional<Boolean> delete(String queryId, String cursorId);
}
