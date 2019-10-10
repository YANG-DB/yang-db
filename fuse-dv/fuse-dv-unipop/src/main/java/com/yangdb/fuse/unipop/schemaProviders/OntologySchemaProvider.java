package com.yangdb.fuse.unipop.schemaProviders;

/*-
 *
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.google.inject.Inject;
import com.yangdb.fuse.model.ontology.*;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.*;

/**
 * Created by benishue on 22-Mar-17.
 */
public class OntologySchemaProvider implements GraphElementSchemaProvider {

    //region Constructor
    @Inject
    public OntologySchemaProvider(Ontology ontology, GraphElementSchemaProvider schemaProvider) {
        this.schemaProvider = schemaProvider;
        this.$ont = new Ontology.Accessor(ontology);

        this.vertexLabels = new HashSet<>(Stream.ofAll($ont.eNames()).toJavaList());
        this.edgeLabels = new HashSet<>(Stream.ofAll($ont.rNames()).toJavaList());
        this.propertyNames = new HashSet<>(Stream.ofAll($ont.pTypes()).toJavaList());
    }
    //endregion

    //region GraphElementSchemaProvider implementation
    @Override
    public Iterable<GraphVertexSchema> getVertexSchemas(String label) {
        return Stream.ofAll(this.schemaProvider.getVertexSchemas(label))
                .map(vertexSchema -> getEntitySchema(label, vertexSchema))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
        return mergeEdgeSchemasWithRelationship(label, this.schemaProvider.getEdgeSchemas(label));
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label) {
        return mergeEdgeSchemasWithRelationship(label, Stream.ofAll(this.schemaProvider.getEdgeSchemas(vertexLabelA, label)));
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label) {
        return mergeEdgeSchemasWithRelationship(label, Stream.ofAll(this.schemaProvider.getEdgeSchemas(vertexLabelA, direction, label)));
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB) {
        return mergeEdgeSchemasWithRelationship(label, Stream.ofAll(this.schemaProvider.getEdgeSchemas(vertexLabelA, direction, label, vertexLabelB)));
    }

    @Override
    public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
        Optional<Property> property = $ont.property(name);
        if (!property.isPresent()) {
            return Optional.empty();
        }

        Optional<GraphElementPropertySchema> propertySchema = this.schemaProvider.getPropertySchema(name);

        return Optional.of(new GraphElementPropertySchema.Impl(
                property.get().getName(),
                property.get().getType(),
                propertySchema.map(GraphElementPropertySchema::getIndexingSchemes)
                        .orElseGet(() -> Collections.singletonList(
                                new GraphElementPropertySchema.ExactIndexingSchema.Impl(property.get().getName())))));
    }

    @Override
    public Iterable<String> getVertexLabels() {
        return this.vertexLabels;
    }

    @Override
    public Iterable<String> getEdgeLabels() {
        return this.edgeLabels;
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return this.propertyNames;
    }
    //endregion

    //region Private Methods
    private Optional<GraphVertexSchema> getEntitySchema(String label, GraphVertexSchema vertexSchema) {
        Optional<EntityType> entityType = $ont.entity(label);
        if (!entityType.isPresent()) {
            return Optional.empty();
        }

        if (GraphVirtualVertexSchema.class.isAssignableFrom(vertexSchema.getClass())) {
            return Optional.of(vertexSchema);
        }

        return Optional.of(new GraphVertexSchema.Impl(
                label,
                vertexSchema.getConstraint(),
                vertexSchema.getRouting(),
                vertexSchema.getIndexPartitions(),
                Stream.ofAll(entityType.get().getProperties() == null ? Collections.emptyList() : entityType.get().getProperties())
                        .map(pType -> $ont.$property(pType))
                        .filter(Optional::isPresent)
                        .map(property -> (GraphElementPropertySchema)
                                (vertexSchema.getProperty(property.get().getName()).isPresent() ?
                                new GraphElementPropertySchema.Impl(
                                        property.get().getName(),
                                        property.get().getType(),
                                        vertexSchema.getProperty(property.get().getName()).get().getIndexingSchemes()) :
                                new GraphElementPropertySchema.Impl(property.get().getName(), property.get().getType())))));
    }

    private Optional<GraphEdgeSchema> getRelationSchema(
            String label,
            String sourceVertexLabel,
            String destinationVertexLabel,
            GraphEdgeSchema edgeSchema) {
        Optional<RelationshipType> relationshipType = $ont.relation(label);
        if (!relationshipType.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new GraphEdgeSchema.Impl(
                label,
                edgeSchema.getConstraint(),
                edgeSchema.getEndA().isPresent() ?
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                edgeSchema.getEndA().get().getIdFields(),
                                edgeSchema.getEndA().get().getLabel().isPresent() ?
                                        edgeSchema.getEndA().get().getLabel() :
                                        Optional.of(sourceVertexLabel),
                                edgeSchema.getEndA().get().getRedundantProperties(),
                                edgeSchema.getEndA().get().getRouting(),
                                edgeSchema.getEndA().get().getIndexPartitions())) :
                        Optional.of(new GraphEdgeSchema.End.Impl(null, Optional.of(sourceVertexLabel))),
                edgeSchema.getEndB().isPresent() ?
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                edgeSchema.getEndB().get().getIdFields(),
                                edgeSchema.getEndB().get().getLabel().isPresent() ?
                                        edgeSchema.getEndB().get().getLabel() :
                                        Optional.of(destinationVertexLabel),
                                edgeSchema.getEndB().get().getRedundantProperties(),
                                edgeSchema.getEndB().get().getRouting(),
                                edgeSchema.getEndB().get().getIndexPartitions())) :
                        Optional.of(new GraphEdgeSchema.End.Impl(null, Optional.of(destinationVertexLabel))),
                edgeSchema.getDirection(),
                edgeSchema.getDirectionSchema(),
                edgeSchema.getRouting(),
                edgeSchema.getIndexPartitions(),
                Stream.ofAll(relationshipType.get().getProperties() == null ? Collections.emptyList() : relationshipType.get().getProperties())
                        .map(pType -> $ont.$property(pType))
                        .filter(property -> property.isPresent())
                        .map(property -> (GraphElementPropertySchema)
                                (edgeSchema.getProperty(property.get().getName()).isPresent() ?
                                        new GraphElementPropertySchema.Impl(
                                                property.get().getName(),
                                                property.get().getType(),
                                                edgeSchema.getProperty(property.get().getName()).get().getIndexingSchemes()) :
                                        new GraphElementPropertySchema.Impl(property.get().getName(), property.get().getType()))),
                edgeSchema.getApplications()
        ));
    }
    //endregion

    private Iterable<GraphEdgeSchema> mergeEdgeSchemasWithRelationship(String label, Iterable<GraphEdgeSchema> edgeSchemas) {
        Optional<RelationshipType> relationshipType = $ont.relation(label);
        if (relationshipType.isPresent()) {
            List<GraphEdgeSchema> graphEdgeSchemas = new ArrayList<>();
            List<EPair> verticesPair = relationshipType.get().getePairs();
            for (EPair ePair : verticesPair) {
                String eTypeA = $ont.$entity$(ePair.geteTypeA()).getName();
                String eTypeB = $ont.$entity$(ePair.geteTypeB()).getName();

                Stream.ofAll(edgeSchemas)
                        .filter(schema -> isEdgeSchemaProperlyDirected(eTypeA, eTypeB, schema))
                        .map(schema -> getRelationSchema(label, eTypeA, eTypeB, schema))
                        .filter(Optional::isPresent)
                        .forEach(schema -> graphEdgeSchemas.add(schema.get()));
            }

            return graphEdgeSchemas;
        }

        return Collections.emptyList();
    }

    private boolean isEdgeSchemaProperlyDirected(String sourceLabel, String destinationLabel, GraphEdgeSchema schema) {
        String schemaSourceLabel = schema.getDirection().equals(Direction.OUT) ?
                schema.getEndA().get().getLabel().get() :
                schema.getEndB().get().getLabel().get();

        String schemaDestinationLabel = schema.getDirection().equals(Direction.OUT) ?
                schema.getEndB().get().getLabel().get() :
                schema.getEndA().get().getLabel().get();

        return sourceLabel.equals(schemaSourceLabel) && destinationLabel.equals(schemaDestinationLabel);
    }
    //endregion

    //region Fields
    private GraphElementSchemaProvider schemaProvider;
    private Ontology.Accessor $ont;

    protected Set<String> vertexLabels;
    protected Set<String> edgeLabels;
    protected Set<String> propertyNames;
    //endregion
}
