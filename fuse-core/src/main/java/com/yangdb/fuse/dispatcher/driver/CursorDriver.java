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
    /**
     * create cursor resource
     * @param queryId
     * @param cursorRequest
     * @return
     */
    Optional<CursorResourceInfo> create(String queryId, CreateCursorRequest cursorRequest);

    /**
     * get cursors resource info according ot query Id
     * @param queryId
     * @return
     */
    Optional<StoreResourceInfo> getInfo(String queryId);

    /**
     * get specific cursor resource info according to the cursor & query ids
     * @param queryId
     * @param cursorId
     * @return
     */
    Optional<CursorResourceInfo> getInfo(String queryId, String cursorId);

    /**
     * get the graph traversal physical plan according to the logical plan
     * @param plan
     * @param ontology
     * @return
     */
    Optional<GraphTraversal> traversal(PlanWithCost plan, String ontology);

    /**
     * delete the cursor resource and its related sub-resources
     * @param queryId
     * @param cursorId
     * @return
     */
    Optional<Boolean> delete(String queryId, String cursorId);
}
