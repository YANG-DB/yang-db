package com.kayhut.fuse.services.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.transport.ContentResponse;

import javax.annotation.Nullable;

/**
 * Created by lior on 19/02/2017.
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
