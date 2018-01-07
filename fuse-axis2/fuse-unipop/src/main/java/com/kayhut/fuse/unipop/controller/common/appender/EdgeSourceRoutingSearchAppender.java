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

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        //currently assuming same vertex labels
        String bulkVertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();
        GraphEdgeSchema.End otherEndSchema = edgeSchema.getSource().get().getLabel().get().equals(bulkVertexLabel) ?
                edgeSchema.getDestination().get() : edgeSchema.getSource().get();
        Optional<GraphVertexSchema> otherVertexScema = context.getSchemaProvider().getVertexSchema(otherEndSchema.getLabel().get());

        if (!otherVertexScema.isPresent() || !otherVertexScema.get().getRouting().isPresent()) {
            return false;
        }

        GraphElementPropertySchema routingProperty = otherVertexScema.get().getRouting().get().getRoutingProperty();
        Optional<GraphRedundantPropertySchema> redundnatRoutingProperty = otherEndSchema.getRedundantProperty(routingProperty);
        if (!redundnatRoutingProperty.isPresent()) {
            return false;
        }

        searchBuilder.getIncludeSourceFields().add(redundnatRoutingProperty.get().getPropertyRedundantName());
        return true;
    }
    //endregion
}
