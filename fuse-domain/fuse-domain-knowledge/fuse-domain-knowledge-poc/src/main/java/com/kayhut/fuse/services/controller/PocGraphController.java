package com.kayhut.fuse.services.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.transport.ContentResponse;
import org.graphstream.graph.Graph;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Created by lior on 19/02/2017.
 */
public interface PocGraphController {

    ContentResponse<ObjectNode> getGraphWithRank(boolean cache, String queryId, String cursorId, String pageId, @Nullable String context);

    ContentResponse<ObjectNode> getGraphWithRank( boolean cache, int top, @Nullable String context);

    ContentResponse<Map> getGraphWithConnectivity(boolean cache, String queryId, String cursorId, String pageId, String context);

}
