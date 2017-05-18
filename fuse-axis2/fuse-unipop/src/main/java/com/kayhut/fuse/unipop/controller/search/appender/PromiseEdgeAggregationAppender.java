package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.controller.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;

/**
 * Created by Elad on 4/26/2017.
 */
public class PromiseEdgeAggregationAppender implements SearchAppender<PromiseVertexControllerContext> {
    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseVertexControllerContext promiseVertexControllerContext) {
        searchBuilder.getAggregationBuilder().seekRoot()
                     .terms(GlobalConstants.EdgeSchema.SOURCE)
                     .field(GlobalConstants.EdgeSchema.SOURCE_ID)
                     .executionHint("map")
                     .terms(GlobalConstants.EdgeSchema.DEST)
                     .field(GlobalConstants.EdgeSchema.DEST_ID)
                     .executionHint("map");
        return true;
    }
}