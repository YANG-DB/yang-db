package com.kayhut.fuse.unipop.schemaProviders;

import com.google.common.base.Strings;
import com.kayhut.fuse.model.ontology.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by benishue on 22-Mar-17.
 */
public class OntologySchemaProvider implements GraphElementSchemaProvider {

    //region Constructor
    public OntologySchemaProvider(Ontology ontology, PhysicalIndexProvider physicalIndexProvider) {
        this(ontology, physicalIndexProvider, GraphLayoutProvider.NoneRedundant.getInstance());
    }

    public OntologySchemaProvider(Ontology ontology, PhysicalIndexProvider physicalIndexProvider, GraphLayoutProvider graphLayoutProvider) {
        this.$ont = new Ontology.Accessor(ontology);
        this.physicalIndexProvider = physicalIndexProvider;
        this.graphLayoutProvider = graphLayoutProvider;

        this.vertexTypes = new HashSet<>(Stream.ofAll($ont.eNames()).toJavaList());
        this.edgeTypes = new HashSet<>(Stream.ofAll($ont.rNames()).toJavaList());
    }
    //endregion

    //region GraphElementSchemaProvider implementation
    @Override
    public Optional<GraphVertexSchema> getVertexSchema(String label) {
        if (Strings.isNullOrEmpty(label) || !this.vertexTypes.contains(label)) {
            return Optional.empty();
        } else
            return getEntityTypeSchema(label);
    }

    @Override
    public Optional<GraphEdgeSchema> getEdgeSchema(
            String label) {

        if (Strings.isNullOrEmpty(label)) {
            return Optional.empty();
        }

        Iterable<GraphEdgeSchema> edgeSchemas = getEdgeSchemas(label);
        if (Stream.ofAll(edgeSchemas).isEmpty())
            return Optional.empty();

        return Optional.of(Stream.ofAll(edgeSchemas).get(0));

        //return Stream.ofAll(edgeSchemas.get()).find(edgeSchema -> edgeSchema.getLabel().equals(edgeType)).toJavaOptional() ;
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
        if (Strings.isNullOrEmpty(label)) {
            return Collections.emptyList();
        }

        Optional<RelationshipType> relationshipType = $ont.relation(label);
        if (relationshipType.isPresent()) {
            List<GraphEdgeSchema> graphEdgeSchemas = new ArrayList<>();
            List<EPair> verticesPair = relationshipType.get().getePairs();
            for (EPair ePair : verticesPair) {
                String eTypeA = $ont.$entity$(ePair.geteTypeA()).getName();
                String eTypeB = $ont.$entity$(ePair.geteTypeB()).getName();
                Optional<GraphEdgeSchema> relationTypeSchema = getRelationTypeSchema(label, eTypeA, eTypeB);
                if (relationTypeSchema.isPresent()) {
                    graphEdgeSchemas.add(relationTypeSchema.get());
                }
            }
            if (graphEdgeSchemas.size() > 0) {
                return graphEdgeSchemas;
            } else {
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
        Optional<Property> property = $ont.property(name);
        if (!property.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new GraphElementPropertySchema() {
            @Override
            public String getName() {
                return property.get().getName();
            }

            @Override
            public String getType() {
                return property.get().getType();
            }
        });
    }

    @Override
    public Iterable<String> getVertexLabels() {
        return this.vertexTypes;
    }

    @Override
    public Iterable<String> getEdgeLabels() {
        return this.edgeTypes;
    }
    //endregion

    //region Private Methods
    private Optional<GraphVertexSchema> getEntityTypeSchema(String vertexType) {
        EntityType entityType = $ont.entity$(vertexType);

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
            public IndexPartitions getIndexPartitions() {
                return physicalIndexProvider.getIndexPartitionByLabel(vertexType, ElementType.vertex);
            }

            @Override
            public Iterable<GraphElementPropertySchema> getProperties() {
                return Stream.ofAll(entityType.getProperties())
                        .map(pType -> $ont.$property$(pType))
                        .map(property -> (GraphElementPropertySchema)new GraphElementPropertySchema() {
                                @Override
                                public String getName() {
                                    return property.getName();
                                }

                                @Override
                                public String getType() {
                                    return property.getType();
                                }
                            }).toJavaList();
            }

            @Override
            public Optional<GraphElementPropertySchema> getProperty(String name) {
                return Stream.ofAll(entityType.getProperties())
                        .map(pType -> $ont.$property$(pType))
                        .filter(property -> property.getName().equals(name))
                        .toJavaOptional()
                        .map(property -> new GraphElementPropertySchema() {
                            @Override
                            public String getName() {
                                return property.getName();
                            }

                            @Override
                            public String getType() {
                                return property.getType();
                            }
                        });
            }
        });
    }

    private Optional<GraphEdgeSchema> getRelationTypeSchema(String edgeType, String sourceVertexType, String destinationVertexType) {
        RelationshipType relationshipType = $ont.relation$(edgeType);
        return Optional.of(new GraphEdgeSchema() {
            @Override
            public Optional<End> getSource() {
                return Optional.of(new End() {
                    @Override
                    public String getIdField() {
                        return sourceVertexType + "IdA";
                    }

                    @Override
                    public Optional<String> getLabel() {
                        return Optional.of(sourceVertexType);
                    }

                    @Override
                    public Optional<GraphRedundantPropertySchema> getRedundantProperty(GraphElementPropertySchema property) {
                        return graphLayoutProvider.getRedundantProperty(edgeType, property);
                    }

                    @Override
                    public Iterable<GraphRedundantPropertySchema> getRedundantProperties() {
                        return Collections.emptyList();
                    }

                    @Override
                    public Optional<GraphElementRouting> getRouting() {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<IndexPartitions> getIndexPartitions() {
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
                    public Optional<String> getLabel() {
                        return Optional.of(destinationVertexType);
                    }

                    @Override
                    public Optional<GraphRedundantPropertySchema> getRedundantProperty(GraphElementPropertySchema property) {
                        return graphLayoutProvider.getRedundantProperty(edgeType, property);
                    }

                    @Override
                    public Iterable<GraphRedundantPropertySchema> getRedundantProperties() {
                        return Collections.emptyList();
                    }

                    @Override
                    public Optional<GraphElementRouting> getRouting() {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<IndexPartitions> getIndexPartitions() {
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
            public IndexPartitions getIndexPartitions() {
                return physicalIndexProvider.getIndexPartitionByLabel(edgeType, ElementType.edge);
            }

            @Override
            public Iterable<GraphElementPropertySchema> getProperties() {
                return Stream.ofAll(relationshipType.getProperties())
                        .map(pType -> $ont.$property$(pType))
                        .map(property -> (GraphElementPropertySchema) new GraphElementPropertySchema() {
                            @Override
                            public String getName() {
                                return property.getName();
                            }

                            @Override
                            public String getType() {
                                return property.getType();

                            }
                        }).toJavaList();
            }

            @Override
            public Optional<GraphElementPropertySchema> getProperty(String name) {
                return Stream.ofAll(relationshipType.getProperties())
                        .map(pType -> $ont.$property$(pType))
                        .filter(property -> property.getName().equals(name))
                        .toJavaOptional()
                        .map(property -> (GraphElementPropertySchema) new GraphElementPropertySchema() {
                            @Override
                            public String getName() {
                                return property.getName();
                            }

                            @Override
                            public String getType() {
                                return property.getType();

                            }
                        });
            }
        });
    }
    //endregion

    //region Fields
    protected PhysicalIndexProvider physicalIndexProvider;
    protected Set<String> vertexTypes;
    protected Set<String> edgeTypes;
    protected Ontology.Accessor $ont;
    protected GraphLayoutProvider graphLayoutProvider = null;
    //endregion
}