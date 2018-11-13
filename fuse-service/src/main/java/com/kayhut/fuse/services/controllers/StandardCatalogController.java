package com.kayhut.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

/**
 * Created by lior.perry on 19/02/2017.
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
    public ContentResponse<List<Ontology>> getOntologies() {
        return Builder.<List<Ontology>>builder(OK, NOT_FOUND)
                .data(Optional.of(Stream.ofAll(this.ontologyProvider.getAll()).toJavaList()))
                .compose();
    }

    @Override
    public ContentResponse<Ontology> getOntology(String id) {
        return Builder.<Ontology>builder(OK, NOT_FOUND)
                .data(ontologyProvider.get(id))
                .compose();
    }

    @Override
    public ContentResponse<GraphElementSchemaProvider> getSchema(String id) {
        Optional<Ontology> ontology = this.ontologyProvider.get(id);
        if (!ontology.isPresent()) {
            return ContentResponse.notFound();
        }

        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(this.ontologyProvider.get(id).get());
        return Builder.<GraphElementSchemaProvider>builder(OK, NOT_FOUND)
                .data(Optional.of(createSerializableSchemaProvider(schemaProvider)))
                .compose();
    }

    @Override
    public ContentResponse<List<GraphElementSchemaProvider>> getSchemas() {
        return Builder.<List<GraphElementSchemaProvider>>builder(OK, NOT_FOUND)
                .data(Optional.of(Stream.ofAll(this.ontologyProvider.getAll())
                        .map(ont -> createSerializableSchemaProvider(this.schemaProviderFactory.get(ont)))
                        .toJavaList()))
                .compose();
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
                            edgeSchema.getEndA(),
                            edgeSchema.getEndB(),
                            edgeSchema.getDirection(),
                            edgeSchema.getDirectionSchema(),
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
