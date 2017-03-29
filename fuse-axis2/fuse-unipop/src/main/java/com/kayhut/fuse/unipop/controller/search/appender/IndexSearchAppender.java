package com.kayhut.fuse.unipop.controller.search.appender;

import com.google.common.collect.Streams;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;

/**
 * Created by User on 27/03/2017.
 */
public class IndexSearchAppender implements SearchAppender<PromiseElementControllerContext> {

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseElementControllerContext promiseElementControllerContext) {
//        GraphElementSchemaProvider schemaProvider = promiseElementControllerContext.getSchemaProvider();
//        promiseElementControllerContext.getConstraint().get();
//        Iterable<String> types = promiseElementControllerContext.getElementType() == ElementType.vertex
//                ? schemaProvider.getVertexTypes() : schemaProvider.getEdgeTypes();
        //searchBuilder.getIndices().add("index1");
        return false;
    }
    //endregion
}
