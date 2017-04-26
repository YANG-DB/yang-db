package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.unipop.controller.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;

/**
 * Created by Elad on 4/26/2017.
 */
public class PromiseEdgeAggregationAppender implements SearchAppender<PromiseVertexControllerContext> {
    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseVertexControllerContext promiseVertexControllerContext) {
        searchBuilder.getAggregationBuilder().seekRoot().terms("layer1").field("entityA.id").executionHint("map").terms("layer2").field("entityB.id").executionHint("map");
        return false;
    }
}
