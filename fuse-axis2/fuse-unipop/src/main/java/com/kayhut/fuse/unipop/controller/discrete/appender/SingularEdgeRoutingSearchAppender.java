package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.controller.utils.ElementUtil;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier.*;

/**
 * Created by roman.margolis on 18/09/2017.
 */
@Deprecated
public class SingularEdgeRoutingSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().singular().applicable().get();
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        GraphEdgeSchema.End endSchema = context.getDirection().equals(Direction.OUT) ?
                edgeSchema.getSource().get() :
                edgeSchema.getDestination().get();

        if (endSchema.getRouting().isPresent()) {
            boolean isRoutingFieldFullyAvailable =
                    Stream.ofAll(context.getBulkVertices())
                            .map(vertex -> ElementUtil.<String>value(vertex,
                                    translateRoutingPropertyName(endSchema.getRouting().get().getRoutingProperty().getName())))
                            .filter(value -> !value.isPresent())
                            .size() == 0;

            Set<String> routingValues = Collections.emptySet();
            if (isRoutingFieldFullyAvailable) {
                routingValues =
                        Stream.ofAll(context.getBulkVertices())
                                .map(vertex -> ElementUtil.<String>value(vertex, translateRoutingPropertyName(endSchema.getRouting().get().getRoutingProperty().getName())))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toJavaSet();
            }

            searchBuilder.getRouting().addAll(routingValues);
            return routingValues.size() > 0;
        }

        return false;
    }
    //endregion

    //region Private Methods
    private String translateRoutingPropertyName(String name) {
        return name.equals("_id") ? T.id.getAccessor() : name;
    }
    //endregion
}
