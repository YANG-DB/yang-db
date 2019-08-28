package com.yangdb.fuse.executor.ontology.schema;

import com.google.inject.Inject;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.Relation;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GraphElementSchemaProviderJsonFactory implements GraphElementSchemaProviderFactory {

    private IndexProvider indexProvider;

    @Inject
    public GraphElementSchemaProviderJsonFactory(IndexProvider indexProvider) {
        this.indexProvider = indexProvider;
    }

    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return new GraphElementSchemaProvider.Impl(
                getVertexSchemas(),
                getEdgeSchemas(),
                getPropertySchemas());
    }

    private List<GraphElementPropertySchema> getPropertySchemas() {
        return new ArrayList<>();
    }

    private List<GraphEdgeSchema> getEdgeSchemas() {
        return indexProvider.getRelations().stream().flatMap(r -> generateGraphEdgeSchema(r).stream()).collect(Collectors.toList());
    }

    private List<GraphEdgeSchema> generateGraphEdgeSchema(Relation r) {
        switch (r.getPartition()) {
            case "static":
                return
                        r.getProps().getValues().stream()
                                .map(v -> new GraphEdgeSchema.Impl(r.getType(), new StaticIndexPartitions(v)))
                                .collect(Collectors.toList());
            case "time":
                //todo
                break;
        }
        return Collections.singletonList(new GraphEdgeSchema.Impl(r.getType(),
                new StaticIndexPartitions(r.getProps().getValues().isEmpty() ? r.getType() : r.getProps().getValues().get(0))));
    }

    private List<GraphVertexSchema> getVertexSchemas() {
        return indexProvider.getEntities().stream().flatMap(e -> generateGraphVertexSchema(e).stream()).collect(Collectors.toList());
    }

    private List<GraphVertexSchema> generateGraphVertexSchema(Entity e) {
        switch (e.getPartition()) {
            case "static":
                return
                        e.getProps().getValues().stream()
                                .map(v -> new GraphVertexSchema.Impl(e.getType(), new StaticIndexPartitions(v)))
                                .collect(Collectors.toList());
            case "time":
                //todo
                break;
        }
        return Collections.singletonList(new GraphVertexSchema.Impl(e.getType(),
                new StaticIndexPartitions(e.getProps().getValues().isEmpty() ? e.getType() : e.getProps().getValues().get(0))));
    }
}
