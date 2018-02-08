package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.model.query.Rel;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Set;

public class EdgeSchemaSupplier {

    public EdgeSchemaSupplier(EdgeSchemaSupplierContext context) {
        this.context = context;
        this.stream = Stream.ofAll(context.getGraphElementSchemaProvider().getEdgeSchemas());
    }

    public EdgeSchemaSupplier labels() {
        this.stream = this.stream.filter(edgeSchema -> this.context.getLabels().contains(edgeSchema.getLabel()));
        return this;
    }

    public EdgeSchemaSupplier applicable() {
        this.stream = this.stream
                .filter(edgeSchema -> (edgeSchema.getApplications().contains(GraphEdgeSchema.Application.source) &&
                        edgeSchema.getSource().get().getLabel().get().equals(context.getVertexLabel())) ||
                        (edgeSchema.getApplications().contains(GraphEdgeSchema.Application.destination) &&
                                edgeSchema.getDestination().get().getLabel().get().equals(context.getVertexLabel())));

        return this;
    }

    public EdgeSchemaSupplier dual() {
        this.stream = this.stream
                .filter(edgeSchema -> edgeSchema.getDirection().isPresent())
                .filter(edgeSchema -> edgeSchema.getSource().get().getLabel().get().equals(context.getVertexLabel()));

        return this;
    }

    public EdgeSchemaSupplier singular() {
        this.stream = this.stream
                .filter(edgeSchema -> !edgeSchema.getDirection().isPresent())
                .filter(edgeSchema -> (edgeSchema.getSource().get().getLabel().get().equals(context.getVertexLabel()) &&
                        (context.getDirection().equals(Rel.Direction.R) || context.getDirection().equals(Rel.Direction.RL))) ||
                        (edgeSchema.getDestination().get().getLabel().get().equals(context.getVertexLabel()) &&
                                (context.getDirection().equals(Rel.Direction.L) || context.getDirection().equals(Rel.Direction.RL))));
        return this;
    }

    public Iterable<GraphEdgeSchema> get() {
        if (this.cache == null) {
            this.cache = this.stream.toJavaList();
        }

        return this.cache;
    }

    private Stream<GraphEdgeSchema> stream;
    private List<GraphEdgeSchema> cache;
    private EdgeSchemaSupplierContext context;
}
