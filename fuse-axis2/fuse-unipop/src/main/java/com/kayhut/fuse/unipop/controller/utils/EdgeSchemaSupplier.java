package com.kayhut.fuse.unipop.controller.utils;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class EdgeSchemaSupplier implements Supplier<Iterable<GraphEdgeSchema>> {

    //region Constructors
    public EdgeSchemaSupplier(VertexControllerContext context) {
        this.context = context;
        this.stream = Stream.ofAll(context.getSchemaProvider().getEdgeLabels())
                .flatMap(label -> context.getSchemaProvider().getEdgeSchemas(label));
    }
    //endregion

    //region Supplier Implementation
    @Override
    public Iterable<GraphEdgeSchema> get() {
        if (this.cache == null) {
            this.cache = this.stream.toJavaList();
        }

        return this.cache;
    }
    //endregion

    //region Public Methods
    public EdgeSchemaSupplier labels() {
        Set<String> labels = this.context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(this.context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        this.stream = this.stream.filter(edgeSchema -> labels.contains(edgeSchema.getLabel()));
        return this;
    }

    public EdgeSchemaSupplier singular() {
        //currently assuming all bulk vertices of same type
        String vertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();

        this.stream = this.stream
                .filter(edgeSchema -> !edgeSchema.getDirectionSchema().isPresent())
                .filter(edgeSchema -> (edgeSchema.getEndA().get().getLabel().get().equals(vertexLabel) &&
                        (context.getDirection().equals(Direction.OUT) || context.getDirection().equals(Direction.BOTH))) ||
                        (edgeSchema.getEndB().get().getLabel().get().equals(vertexLabel) &&
                                (context.getDirection().equals(Direction.IN) || context.getDirection().equals(Direction.BOTH))));
        return this;
    }

    public EdgeSchemaSupplier dual() {
        //currently assuming all bulk vertices of same type
        String vertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();

        this.stream = this.stream
                .filter(edgeSchema -> edgeSchema.getDirectionSchema().isPresent())
                .filter(edgeSchema -> edgeSchema.getEndA().get().getLabel().get().equals(vertexLabel));

        return this;
    }

    public EdgeSchemaSupplier applicable() {
        //currently assuming all bulk vertices of same type
        String vertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();

        this.stream = this.stream
                .filter(edgeSchema -> (edgeSchema.getApplications().contains(GraphEdgeSchema.Application.endA) &&
                edgeSchema.getEndA().get().getLabel().get().equals(vertexLabel)) ||
                (edgeSchema.getApplications().contains(GraphEdgeSchema.Application.endB) &&
                        edgeSchema.getEndB().get().getLabel().get().equals(vertexLabel)));

        return this;
    }
    //endregion

    //region Fields
    private VertexControllerContext context;
    private Stream<GraphEdgeSchema> stream;
    private List<GraphEdgeSchema> cache;
    //endregion
}
