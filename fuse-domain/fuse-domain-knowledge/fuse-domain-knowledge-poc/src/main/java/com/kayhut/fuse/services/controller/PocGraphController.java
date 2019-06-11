package com.kayhut.fuse.services.controller;

/*-
 * #%L
 * fuse-domain-knowledge-poc
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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.transport.ContentResponse;

import javax.annotation.Nullable;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface PocGraphController {

    ContentResponse<ObjectNode> getGraphWithRank(boolean cache, String queryId, String cursorId, String pageId, @Nullable String context);

    ContentResponse<ObjectNode> getGraphWithRank(boolean cache, int top, @Nullable String context, String category);

    ContentResponse<String> getGraphWithRankReport(boolean cache, int top, @Nullable String context, String category, String... headers);

    ContentResponse<ObjectNode> getGraphPath(boolean cache, String sourceNodeId, String targetNodeId, String context);

    ContentResponse<ObjectNode> getGraphWithConnectedComponents(boolean cache, int topN, @Nullable String context);

    ContentResponse<ObjectNode> getGraphWithCentroid(boolean cache, int topN, @Nullable String context);

    ContentResponse<ObjectNode> getGraphWithConnectivity(boolean cache, @Nullable String context);

    ContentResponse<ObjectNode> getGraphWithEccentricity(boolean cache, @Nullable String context);
}
