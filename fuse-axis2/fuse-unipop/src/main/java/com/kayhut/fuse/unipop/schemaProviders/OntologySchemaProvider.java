package com.kayhut.fuse.unipop.schemaProviders;

import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import com.kayhut.fuse.model.ontology.EPair;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;

import java.util.*;

/**
 * Created by benishue on 22-Mar-17.
 */
public class OntologySchemaProvider implements GraphElementSchemaProvider {

    //region Constructor
    public OntologySchemaProvider(PhysicalIndexProvider indexProvider, Ontology ontology) {
        this.indexProvider = indexProvider;
        this.ontology = ontology;
        this.vertexTypes = new HashSet<>(OntologyUtil.getAllEntityLabels(ontology).get());
        this.edgeTypes = new HashSet<>(OntologyUtil.getAllRelationshipTypeLabels(ontology).get());
    }
    //endregion

    //region GraphElementSchemaProvider implementation
    @Override
    public Optional<GraphVertexSchema> getVertexSchema(String vertexType) {
        if (Strings.isNullOrEmpty(vertexType) || !this.vertexTypes.contains(vertexType)) {
            return Optional.empty();
        } else
            return getEntityTypeSchema(vertexType);
    }

    @Override
    public Optional<GraphEdgeSchema> getEdgeSchema(
            String edgeType,
            Optional<String> sourceVertexType,
            Optional<String> destinationVertexType) {

        if (Strings.isNullOrEmpty(edgeType)) {
            return Optional.empty();
        }

        Optional<Iterable<GraphEdgeSchema>> edgeSchemas = getEdgeSchemas(edgeType);
        if (!edgeSchemas.isPresent())
            return Optional.empty();

        Optional<GraphEdgeSchema> graphEdgeSchema = Streams.stream(edgeSchemas.get()).filter(edgeSchema -> edgeSchema.getType().equals(edgeType) &&
                edgeSchema.getSource().get().getType().get().equals(sourceVertexType.get()) &&
                edgeSchema.getDestination().get().getType().get().equals(destinationVertexType.get())).findFirst();

        if (!graphEdgeSchema.isPresent())
            return Optional.empty();

        return graphEdgeSchema;
    }

    @Override
    public Optional<Iterable<GraphEdgeSchema>> getEdgeSchemas(String edgeType) {
        if (Strings.isNullOrEmpty(edgeType)) {
            return Optional.empty();
        }

        Optional<RelationshipType> relationshipType = OntologyUtil.getRelationshipType(ontology, edgeType);
        if (relationshipType.isPresent()) {
            List<GraphEdgeSchema> graphEdgeSchemas = new ArrayList<>();
            List<EPair> verticesPair = relationshipType.get().getePairs();
            for (EPair ePair : verticesPair) {
                String eTypeA = OntologyUtil.getEntityLabel(ontology, ePair.geteTypeA()).get();
                String eTypeB = OntologyUtil.getEntityLabel(ontology, ePair.geteTypeB()).get();
                Optional<GraphEdgeSchema> relationTypeSchema = getRelationTypeSchema(edgeType, eTypeA, eTypeB);
                if (relationTypeSchema.isPresent()) {
                    graphEdgeSchemas.add(relationTypeSchema.get());
                }
            }
            if (graphEdgeSchemas.size() > 0)
                return Optional.of(graphEdgeSchemas);
            else
                return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public Iterable<String> getVertexTypes() {
        return this.vertexTypes;
    }

    @Override
    public Iterable<String> getEdgeTypes() {
        return this.edgeTypes;
    }
    //endregion

    //region Private Methods
    private Optional<GraphVertexSchema> getEntityTypeSchema(String vertexType) {
        return Optional.of(new GraphVertexSchema() {
            @Override
            public String getType() {
                return vertexType;
            }

            @Override
            public Optional<GraphElementRouting> getRouting() {
                return Optional.empty();
            }

            @Override
            public Iterable<IndexPartition> getIndexPartitions() {
                return indexProvider.getIndexPartitionsByLabel(vertexType, PhysicalIndexProvider.ElementType.vertex);
            }
        });
    }

    private Optional<GraphEdgeSchema> getRelationTypeSchema(String edgeType, String sourceVertexType, String destinationVertexType) {
        return Optional.of(new GraphEdgeSchema() {
            @Override
            public Optional<End> getSource() {
                return Optional.of(new End() {
                    @Override
                    public String getIdField() {
                        return sourceVertexType + "IdA";
                    }

                    @Override
                    public Optional<String> getType() {
                        return Optional.of(sourceVertexType);
                    }

                    @Override
                    public Optional<GraphEdgeRedundancy> getEdgeRedundancy() {
                        return Optional.empty();
                    }
                });
            }

            @Override
            public Optional<End> getDestination() {
                return Optional.of(new End() {
                    @Override
                    public String getIdField() {
                        return destinationVertexType + "IdB";
                    }

                    @Override
                    public Optional<String> getType() {
                        return Optional.of(destinationVertexType);
                    }

                    @Override
                    public Optional<GraphEdgeRedundancy> getEdgeRedundancy() {
                        return Optional.empty();
                    }
                });
            }

            @Override
            public Optional<Direction> getDirection() {
                return Optional.empty();
            }

            @Override
            public String getType() {
                return edgeType;
            }

            @Override
            public Optional<GraphElementRouting> getRouting() {
                return Optional.empty();
            }

            @Override
            public Iterable<IndexPartition> getIndexPartitions() {
                return indexProvider.getIndexPartitionsByLabel(edgeType, PhysicalIndexProvider.ElementType.edge);
            }
        });
    }
    //endregion

    //region Fields
    protected PhysicalIndexProvider indexProvider;
    protected Set<String> vertexTypes;
    protected Set<String> edgeTypes;
    protected Ontology ontology;
    //endregion
}