package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.promise.context.PromiseVertexFilterControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 12/06/2017.
 */
public class FilterIndexSearchAppender implements SearchAppender<PromiseVertexFilterControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseVertexFilterControllerContext context) {
        List<String> indices = Stream.ofAll(context.getStartVertices())
                .map(vertex -> (PromiseVertex)vertex)
                .map(PromiseVertex::getPromise)
                .map(promise -> (IdPromise)promise)
                .map(promise -> promise.getLabel().get())
                .map(label -> context.getSchema().getVertexSchema(label))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(GraphElementSchema::getIndexPartition)
                .flatMap(IndexPartition::getIndices)
                .distinct()
                .toJavaList();

        searchBuilder.getIndices().addAll(indices);
        return true;
    }
    //endregion
}
