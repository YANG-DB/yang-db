package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;

import java.util.Optional;

/**
 * Created by roman.margolis on 03/01/2018.
 */
public class EdgeSourceRoutingSearchAppender  implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().applicable().get();
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming same vertex labels
        String bulkVertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        GraphEdgeSchema.End otherEndSchema = edgeSchema.getEndA().get().getLabel().get().equals(bulkVertexLabel) ?
                edgeSchema.getEndB().get() :
                edgeSchema.getEndA().get();

        Iterable<GraphVertexSchema> otherVertexSchemas = context.getSchemaProvider().getVertexSchemas(otherEndSchema.getLabel().get());

        if (!Stream.ofAll(otherVertexSchemas).isEmpty()) {
            return false;
        }

        GraphVertexSchema otherVertexSchema = Stream.ofAll(otherVertexSchemas).get(0);
        if (!otherVertexSchema.getRouting().isPresent()) {
            return false;
        }


        GraphElementPropertySchema routingProperty = otherVertexSchema.getRouting().get().getRoutingProperty();
        Optional<GraphRedundantPropertySchema> redundnatRoutingProperty = otherEndSchema.getRedundantProperty(routingProperty);
        if (!redundnatRoutingProperty.isPresent()) {
            return false;
        }

        searchBuilder.getIncludeSourceFields().add(redundnatRoutingProperty.get().getPropertyRedundantName());
        return true;
    }
    //endregion
}
