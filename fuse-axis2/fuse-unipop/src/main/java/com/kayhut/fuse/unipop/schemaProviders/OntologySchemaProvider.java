package com.kayhut.fuse.unipop.schemaProviders;

import com.google.common.base.Strings;
import com.kayhut.fuse.model.ontology.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by benishue on 22-Mar-17.
 */
public class OntologySchemaProvider implements GraphElementSchemaProvider {

    //region Constructor
    public OntologySchemaProvider(Ontology ontology, GraphElementSchemaProvider schemaProvider) {
        this.schemaProvider = schemaProvider;
        this.$ont = new Ontology.Accessor(ontology);

        this.vertexLabels = new HashSet<>(Stream.ofAll($ont.eNames()).toJavaList());
        this.edgeLabels = new HashSet<>(Stream.ofAll($ont.rNames()).toJavaList());
    }
    //endregion

    //region GraphElementSchemaProvider implementation
    @Override
    public Optional<GraphVertexSchema> getVertexSchema(String label) {
        Optional<GraphVertexSchema> vertexSchema = this.schemaProvider.getVertexSchema(label);
        return vertexSchema.flatMap(graphVertexSchema -> getEntitySchema(label, graphVertexSchema));

    }

    @Override
    public Optional<GraphEdgeSchema> getEdgeSchema(String label) {
        Optional<GraphEdgeSchema> edgeSchema = this.schemaProvider.getEdgeSchema(label);
        if (!edgeSchema.isPresent()) {
            return Optional.empty();
        }

        Optional<RelationshipType> relationshipType = $ont.relation(label);
        if (relationshipType.isPresent()) {
            EPair ePair = relationshipType.get().getePairs().get(0);
            String eTypeA = $ont.$entity$(ePair.geteTypeA()).getName();
            String eTypeB = $ont.$entity$(ePair.geteTypeB()).getName();
            return getRelationSchema(label, eTypeA, eTypeB, edgeSchema.get());
        }

        return Optional.empty();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
        Iterable<GraphEdgeSchema> edgeSchemas = this.schemaProvider.getEdgeSchemas(label);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            Optional<GraphEdgeSchema> edgeSchema = this.schemaProvider.getEdgeSchema(label);
            if (edgeSchema.isPresent()) {
                edgeSchemas = Collections.singletonList(edgeSchema.get());
            } else {
                return Collections.emptyList();
            }
        }

        Optional<RelationshipType> relationshipType = $ont.relation(label);
        if (relationshipType.isPresent()) {
            List<GraphEdgeSchema> graphEdgeSchemas = new ArrayList<>();
            List<EPair> verticesPair = relationshipType.get().getePairs();
            for (EPair ePair : verticesPair) {
                String eTypeA = $ont.$entity$(ePair.geteTypeA()).getName();
                String eTypeB = $ont.$entity$(ePair.geteTypeB()).getName();

                Optional<GraphEdgeSchema> relevantEdgeSchema =
                        Stream.ofAll(edgeSchemas)
                                .filter(schema -> isEdgeSchemaProperlyDirected(eTypeA, eTypeB, schema))
                                .toJavaOptional();

                if (relevantEdgeSchema.isPresent()) {
                    Optional<GraphEdgeSchema> relationTypeSchema = getRelationSchema(label, eTypeA, eTypeB, relevantEdgeSchema.get());
                    relationTypeSchema.ifPresent(graphEdgeSchemas::add);
                }
            }

            return graphEdgeSchemas;
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
        return this.vertexLabels;
    }

    @Override
    public Iterable<String> getEdgeLabels() {
        return this.edgeLabels;
    }
    //endregion

    //region Private Methods
    private Optional<GraphVertexSchema> getEntitySchema(String label, GraphVertexSchema vertexSchema) {
        Optional<EntityType> entityType = $ont.entity(label);
        if (!entityType.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new GraphVertexSchema.Impl(
                label,
                vertexSchema.getConstraint(),
                vertexSchema.getRouting(),
                vertexSchema.getIndexPartitions(),
                Stream.ofAll(entityType.get().getProperties() == null ? Collections.emptyList() : entityType.get().getProperties())
                        .map(pType -> $ont.$property(pType))
                        .filter(property -> property.isPresent())
                        .map(property -> (GraphElementPropertySchema)
                                new GraphElementPropertySchema.Impl(
                                        property.get().getName(),
                                        property.get().getType()))
                        .toJavaList()
        ));
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
                edgeSchema.getSource().isPresent() ?
                        Optional.of(new GraphEdgeSchema.End.Impl(
                            edgeSchema.getSource().get().getIdField(),
                            Optional.of(sourceVertexLabel),
                            edgeSchema.getSource().get().getRedundantProperties(),
                            edgeSchema.getSource().get().getRouting(),
                            edgeSchema.getSource().get().getIndexPartitions())) :
                        Optional.of(new GraphEdgeSchema.End.Impl(null, Optional.of(sourceVertexLabel))),
                edgeSchema.getDestination().isPresent() ?
                        Optional.of(new GraphEdgeSchema.End.Impl(
                        edgeSchema.getDestination().get().getIdField(),
                        Optional.of(destinationVertexLabel),
                        edgeSchema.getDestination().get().getRedundantProperties(),
                        edgeSchema.getDestination().get().getRouting(),
                        edgeSchema.getDestination().get().getIndexPartitions())) :
                        Optional.of(new GraphEdgeSchema.End.Impl(null, Optional.of(destinationVertexLabel))),
                edgeSchema.getDirection(),
                edgeSchema.getRouting(),
                edgeSchema.getIndexPartitions(),
                Stream.ofAll(relationshipType.get().getProperties() == null ? Collections.emptyList() : relationshipType.get().getProperties())
                        .map(pType -> $ont.$property(pType))
                        .filter(property -> property.isPresent())
                        .map(property -> (GraphElementPropertySchema)
                                new GraphElementPropertySchema.Impl(
                                        property.get().getName(),
                                        property.get().getType()))
                        .toJavaList()
        ));
    }
    //endregion

    //region Private Methods
    private boolean isEdgeSchemaProperlyDirected(String sourceLabel, String destinationLabel, GraphEdgeSchema schema) {
        if (schema.getDirection().isPresent()) {
            List<String> labels = Arrays.asList(schema.getSource().get().getLabel().get(), schema.getDestination().get().getLabel().get());
            return labels.contains(sourceLabel) && labels.contains(destinationLabel);
        } else {
            return schema.getSource().get().getLabel().get().equals(sourceLabel) &&
                    schema.getDestination().get().getLabel().get().equals(destinationLabel);
        }
    }
    //endregion

    //region Fields
    private GraphElementSchemaProvider schemaProvider;
    private Ontology.Accessor $ont;

    protected Set<String> vertexLabels;
    protected Set<String> edgeLabels;
    //endregion

    //region AdapterSchema
    public static class Adapter implements GraphElementSchemaProvider {
        //region Constructors
        public Adapter(Iterable<GraphVertexSchema> vertexSchemas, Iterable<GraphEdgeSchema> edgeSchemas) {
            this(vertexSchemas, Optional.empty(), edgeSchemas, Optional.empty());
        }

        public Adapter(Optional<GraphVertexSchema> defaultVertexSchema,
                       Optional<GraphEdgeSchema> defaultEdgeSchema) {
            this(Collections.emptyList(), defaultVertexSchema, Collections.emptyList(), defaultEdgeSchema);
        }

        public Adapter(Iterable<GraphVertexSchema> vertexSchemas,
                       Optional<GraphVertexSchema> defaultVertexSchema,
                       Iterable<GraphEdgeSchema> edgeSchemas) {
            this(vertexSchemas, defaultVertexSchema, edgeSchemas, Optional.empty());
        }

        public Adapter(Iterable<GraphVertexSchema> vertexSchemas,
                       Iterable<GraphEdgeSchema> edgeSchemas,
                       Optional<GraphEdgeSchema> defaultEdgeSchema) {
            this(vertexSchemas, Optional.empty(), edgeSchemas, defaultEdgeSchema);
        }

        public Adapter(Iterable<GraphVertexSchema> vertexSchemas,
                       Optional<GraphVertexSchema> defaultVertexSchema,
                       Iterable<GraphEdgeSchema> edgeSchemas,
                       Optional<GraphEdgeSchema> defaultEdgeSchema) {
            this.vertexSchemas = Stream.ofAll(vertexSchemas).toJavaMap(schema -> new Tuple2<>(schema.getLabel(), schema));
            this.defaultVertexSchema = defaultVertexSchema;

            this.edgeSchemas = Stream.ofAll(edgeSchemas).groupBy(schema -> schema.getLabel())
                    .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));
            this.defaultEdgeSchema = defaultEdgeSchema;
        }
        //endregion

        //region GraphElementSchemaProvider Implementation
        @Override
        public Optional<GraphVertexSchema> getVertexSchema(String label) {
            Optional<GraphVertexSchema> schema = Optional.ofNullable(this.vertexSchemas.get(label));
            return schema.isPresent() ? schema : defaultVertexSchema;
        }

        @Override
        public Optional<GraphEdgeSchema> getEdgeSchema(String label) {
            Iterable<GraphEdgeSchema> schemas = this.edgeSchemas.get(label);
            if (schemas == null) {
                return defaultEdgeSchema;
            }

            return Optional.of(Stream.ofAll(schemas).get(0));
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
            Iterable<GraphEdgeSchema> schemas = this.edgeSchemas.get(label);
            if (schemas == null) {
                return defaultEdgeSchema.map(Collections::singletonList).orElseGet(Collections::emptyList);
            }

            return schemas;
        }

        @Override
        public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
            return Optional.empty();
        }

        @Override
        public Iterable<String> getVertexLabels() {
            return Collections.emptyList();
        }

        @Override
        public Iterable<String> getEdgeLabels() {
            return Collections.emptyList();
        }
        //endregion

        //region Fields
        private Map<String, GraphVertexSchema> vertexSchemas;
        private Optional<GraphVertexSchema> defaultVertexSchema;

        private Map<String, Iterable<GraphEdgeSchema>> edgeSchemas;
        private Optional<GraphEdgeSchema> defaultEdgeSchema;
        //endregion
    }
    //endregions
}