package com.kayhut.fuse.services.controllers;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ContentResponse.Builder;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementConstraint;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

/**
 * Created by lior on 19/02/2017.
 */
public class StandardCatalogController implements CatalogController {
    //region Constructors
    @Inject
    public StandardCatalogController(OntologyProvider ontologyProvider,
                                     GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region CatalogController Implementation

    @Override
    public List<ContentResponse<Ontology>> getOntologies() {
        return ontologyProvider.getAll().stream().map(ont ->
                Builder.<Ontology>builder(randomUUID().toString(),OK, NOT_FOUND).data(Optional.of(ont)).compose()).
                collect(Collectors.toList());
    }

    @Override
    public ContentResponse<Ontology> getOntology(String id) {
        return Builder.<Ontology>builder(randomUUID().toString(),OK, NOT_FOUND)
                .data(ontologyProvider.get(id))
                .compose();
    }

    @Override
    public ContentResponse<GraphElementSchemaProvider> getSchema(String id) {
        Optional<Ontology> ontology = this.ontologyProvider.get(id);
        if (!ontology.isPresent()) {
            return Builder.<GraphElementSchemaProvider>builder(randomUUID().toString(),OK, NOT_FOUND)
                    .data(Optional.empty())
                    .compose();
        }

        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(this.ontologyProvider.get(id).get());
        return Builder.<GraphElementSchemaProvider>builder(randomUUID().toString(), OK, NOT_FOUND)
                .data(Optional.of(createSerializableSchemaProvider(schemaProvider)))
                .compose();
    }

    @Override
    public List<ContentResponse> getSchemas() {
        List<ContentResponse<Ontology>> ontologies = this.getOntologies();

        return ontologies.stream().map(ont ->
                Builder.<GraphElementSchemaProvider>builder(randomUUID().toString(), OK, NOT_FOUND)
                        .data(Optional.of(createSerializableSchemaProvider(this.schemaProviderFactory.get(ont.getData()))))
                        .compose()).collect(Collectors.toList());
    }
    //endregion

    //region Private Methods
    private GraphElementSchemaProvider createSerializableSchemaProvider(GraphElementSchemaProvider schemaProvider) {
        return new GraphElementSchemaProvider.Impl(
                Stream.ofAll(schemaProvider.getVertexSchemas())
                    .map(vertexSchema -> (GraphVertexSchema)new GraphVertexSchema.Impl(
                            vertexSchema.getLabel(),
                            new GraphElementConstraint.Impl(
                                    new TraversalToString(vertexSchema.getConstraint().getTraversalConstraint().toString())),
                            vertexSchema.getRouting(),
                            vertexSchema.getIndexPartitions(),
                            vertexSchema.getProperties()))
                    .toJavaList(),
                Stream.ofAll(schemaProvider.getEdgeSchemas())
                    .map(edgeSchema -> (GraphEdgeSchema)new GraphEdgeSchema.Impl(
                            edgeSchema.getLabel(),
                            new GraphElementConstraint.Impl(
                                    new TraversalToString(edgeSchema.getConstraint().getTraversalConstraint().toString())),
                            edgeSchema.getSource(),
                            edgeSchema.getDestination(),
                            edgeSchema.getDirection(),
                            edgeSchema.getRouting(),
                            edgeSchema.getIndexPartitions(),
                            edgeSchema.getProperties(),
                            edgeSchema.getApplications()))
                    .toJavaList()
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion

    public static class TraversalToString implements org.apache.tinkerpop.gremlin.process.traversal.Traversal {
        //region Constructors
        public TraversalToString(String traversalToString) {
            this.traversal = traversalToString;
        }
        //endregion

        //region Dummy Implementation
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }
        //endregion

        public String getTraversal() {
            return this.traversal;
        }

        private String traversal;
    }
}
