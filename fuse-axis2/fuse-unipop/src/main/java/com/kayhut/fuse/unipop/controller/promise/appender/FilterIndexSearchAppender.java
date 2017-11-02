package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseVertexFilterControllerContext;
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
public class FilterIndexSearchAppender implements SearchAppender<PromiseVertexFilterControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseVertexFilterControllerContext context) {
        Collection<String> indices = Stream.ofAll(context.getBulkVertices())
                .map(vertex -> (PromiseVertex)vertex)
                .map(PromiseVertex::getPromise)
                .map(promise -> (IdPromise)promise)
                .map(promise -> promise.getLabel().get())
                .map(label -> context.getSchemaProvider().getVertexSchema(label))
                .filter(Optional::isPresent)
                .map(Optional::get)
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
