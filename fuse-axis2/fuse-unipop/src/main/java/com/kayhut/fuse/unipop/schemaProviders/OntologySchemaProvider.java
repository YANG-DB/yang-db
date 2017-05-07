package com.kayhut.fuse.unipop.schemaProviders;

import com.google.common.base.Strings;
import com.kayhut.fuse.model.ontology.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

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
            String edgeType) {

        if (Strings.isNullOrEmpty(edgeType)) {
            return Optional.empty();
        }

        Optional<Iterable<GraphEdgeSchema>> edgeSchemas = getEdgeSchemas(edgeType);
        if (!edgeSchemas.isPresent())
            return Optional.empty();

        return Stream.ofAll(edgeSchemas.get()).find(edgeSchema -> edgeSchema.getType().equals(edgeType)).toJavaOptional() ;
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
        EntityType entityType = ontology.getEntityTypes().stream().filter(tp -> tp.getName().equals(vertexType)).findFirst().get();

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
            public IndexPartition getIndexPartition() {
                return indexProvider.getIndexPartitionByLabel(vertexType, ElementType.vertex);
            }

            @Override
            public Iterable<GraphElementPropertySchema> getProperties() {
                return entityType.getProperties().stream().map(prop -> new GraphElementPropertySchema() {
                    @Override
                    public String getName() {
                        return prop.getName();
                    }

                    @Override
                    public String getType() {
                        return prop.getType();
                    }
                }).collect(Collectors.toList());
            }

            @Override
            public Optional<GraphElementPropertySchema> getProperty(String name) {
                Optional<Property> firstProperty = entityType.getProperties().stream().filter(property -> property.getName().equals(name)).findFirst();
                if(firstProperty.isPresent()){
                    return Optional.of(new GraphElementPropertySchema() {
                        @Override
                        public String getName() {
                            return firstProperty.get().getName();
                        }

                        @Override
                        public String getType() {
                            return firstProperty.get().getType();
                        }
                    });
                }else{
                    return Optional.empty();
                }
            }
        });
    }

    private Optional<GraphEdgeSchema> getRelationTypeSchema(String edgeType, String sourceVertexType, String destinationVertexType) {
        RelationshipType relationshipType = OntologyUtil.getRelationshipType(ontology, edgeType).get();
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
            public IndexPartition getIndexPartition() {
                return indexProvider.getIndexPartitionByLabel(edgeType, ElementType.edge);
            }

            @Override
            public Iterable<GraphElementPropertySchema> getProperties() {
                return relationshipType.getProperties().stream().map(prop -> new GraphElementPropertySchema() {
                    @Override
                    public String getName() {
                        return prop.getName();
                    }

                    @Override
                    public String getType() {
                        return prop.getType();
                    }
                }).collect(Collectors.toList());
            }

            @Override
            public Optional<GraphElementPropertySchema> getProperty(String name) {
                Optional<Property> firstProperty = relationshipType.getProperties().stream().filter(property -> property.getName().equals(name)).findFirst();
                if(firstProperty.isPresent()){
                    return Optional.of(new GraphElementPropertySchema() {
                        @Override
                        public String getName() {
                            return firstProperty.get().getName();
                        }

                        @Override
                        public String getType() {
                            return firstProperty.get().getType();
                        }
                    });
                }else{
                    return Optional.empty();
                }
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