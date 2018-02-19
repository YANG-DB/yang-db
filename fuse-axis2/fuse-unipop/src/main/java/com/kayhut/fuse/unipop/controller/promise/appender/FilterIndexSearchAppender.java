package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.structure.promise.PromiseVertex;
import javaslang.collection.Stream;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by Roman on 12/06/2017.
 */
public class FilterIndexSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Collection<String> indices = Stream.ofAll(context.getBulkVertices())
                .map(vertex -> (PromiseVertex)vertex)
                .map(PromiseVertex::getPromise)
                .map(promise -> (IdPromise)promise)
                .map(promise -> promise.getLabel().get())
                .flatMap(label -> context.getSchemaProvider().getVertexSchemas(label))
                .map(schema -> schema.getIndexPartitions().get())
                .flatMap(IndexPartitions::getPartitions)
                .flatMap(IndexPartitions.Partition::getIndices)
                .distinct()
                .toJavaList();

        searchBuilder.getIndices().addAll(indices);
        return true;
    }
    //endregion
}
