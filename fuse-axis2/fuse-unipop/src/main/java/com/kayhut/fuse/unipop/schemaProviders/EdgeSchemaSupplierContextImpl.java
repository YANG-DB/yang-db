package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.model.query.Rel;

import java.util.Set;

public class EdgeSchemaSupplierContextImpl implements EdgeSchemaSupplierContext {

    public EdgeSchemaSupplierContextImpl(Set<String> labels, Rel.Direction direction, String vertexLabel, GraphElementSchemaProvider graphElementSchemaProvider) {
        this.labels = labels;
        this.direction = direction;
        this.vertexLabel = vertexLabel;
        this.graphElementSchemaProvider = graphElementSchemaProvider;
    }

    @Override
    public Set<String> getLabels() {
        return this.labels;
    }

    @Override
    public Rel.Direction getDirection() {
        return this.direction;
    }

    @Override
    public String getVertexLabel() {
        return this.vertexLabel;
    }

    @Override
    public GraphElementSchemaProvider getGraphElementSchemaProvider() {
        return this.graphElementSchemaProvider;
    }

    private Set<String> labels;
    private Rel.Direction direction;
    private String vertexLabel;
    private GraphElementSchemaProvider graphElementSchemaProvider;

}
