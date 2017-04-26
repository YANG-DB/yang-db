package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.unipop.controller.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.PromiseEdgeConstants;

/**
 * Created by Elad on 4/26/2017.
 */
public class PromiseEdgeAggregationAppender implements SearchAppender<PromiseVertexControllerContext> {
    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseVertexControllerContext promiseVertexControllerContext) {
        searchBuilder.getAggregationBuilder().seekRoot()
                     .terms(PromiseEdgeConstants.SOURCE_AGGREGATION_LAYER)
                     .field(PromiseEdgeConstants.EDGE_SOURCE_ID_FIELD)
                     .executionHint("map")
                     .terms(PromiseEdgeConstants.DEST_AGGREGATION_LAYER)
                     .field(PromiseEdgeConstants.EDGE_DEST_ID_FIELD)
                     .executionHint("map");
        return true;
    }
}
