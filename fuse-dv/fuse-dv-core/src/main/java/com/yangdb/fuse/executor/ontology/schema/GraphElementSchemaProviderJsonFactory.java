package com.yangdb.fuse.executor.ontology.schema;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderIfc;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.elasticsearch.MappingIndexType;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.ontology.EPair;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.*;
import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.NestedIndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endA;
import static java.util.stream.Stream.concat;

public class GraphElementSchemaProviderJsonFactory implements GraphElementSchemaProviderFactory {

    public static final String KEYWORD = "keyword";
    public static final String TEXT = "text";
    public static final String _ID = "_id";

    public static final String ID = "id";
    public static final String ENTITY_A = "entityA";
    public static final String ENTITY_A_ID = "entityA.id";
    public static final String ENTITY_B = "entityB";
    public static final String ENTITY_B_ID = "entityB.id";
    public static final String DIRECTION = "direction";
    public static final String OUT = "out";
    public static final String IN = "in";

    private IndexProvider indexProvider;
    private Ontology.Accessor accessor;

    @Inject
    public GraphElementSchemaProviderJsonFactory(Config config, IndexProviderIfc indexProvider, OntologyProvider ontologyProvider) {
        String assembly = config.getString("assembly");
        this.indexProvider = indexProvider.get(assembly).orElseThrow(() ->
                new FuseError.FuseErrorException(new FuseError("No Index Provider present for assembly ", "No Index Provider  present for assembly " + assembly)));
        this.accessor = new Ontology.Accessor(ontologyProvider.get(assembly).orElseThrow(() ->
                new FuseError.FuseErrorException(new FuseError("No Ontology present for assembly", "No Ontology present for assembly" + assembly))));
    }

    public GraphElementSchemaProviderJsonFactory(IndexProviderIfc indexProviderFactory, Ontology ontology) {
        this.accessor = new Ontology.Accessor(ontology);
        this.indexProvider = indexProviderFactory.get(ontology.getOnt())
                .orElseGet(() -> IndexProvider.Builder.generate(ontology));
    }

    public GraphElementSchemaProviderJsonFactory(IndexProvider indexProvider, Ontology ontology) {
        this.accessor = new Ontology.Accessor(ontology);
        this.indexProvider = indexProvider;
    }

    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return new GraphElementSchemaProvider.Impl(
                getVertexSchemas(),
                getEdgeSchemas());
    }

    private List<GraphEdgeSchema> getEdgeSchemas() {
        return indexProvider.getRelations().stream()
                .flatMap(r -> generateGraphEdgeSchema(r).stream())
                .collect(Collectors.toList());
    }

    private List<GraphEdgeSchema> generateGraphEdgeSchema(Relation r) {
        MappingIndexType type = MappingIndexType.valueOf(r.getPartition().toUpperCase());
        switch (type) {
            case UNIFIED:
                //todo verify correctness
                return r.getProps().getValues().stream()
                        .flatMap(v -> generateGraphEdgeSchema(r, r.getType(), new StaticIndexPartitions(v)).stream())
                        .collect(Collectors.toList());
            case STATIC:
                return r.getProps().getValues().stream()
                        .flatMap(v -> generateGraphEdgeSchema(r, r.getType(), new StaticIndexPartitions(v)).stream())
                        .collect(Collectors.toList());
            case NESTED:
                return r.getProps().getValues().stream()
                        .flatMap(v -> generateGraphEdgeSchema(r, r.getType(), new NestedIndexPartitions(v)).stream())
                        .collect(Collectors.toList());
            case TIME:
                return generateGraphEdgeSchema(r, r.getType(), new TimeBasedIndexPartitions(r.getProps()));
        }

        return Collections.singletonList(new GraphEdgeSchema.Impl(r.getType(),
                new StaticIndexPartitions(r.getProps().getValues().isEmpty() ? r.getType() : r.getProps().getValues().get(0))));
    }


    private List<GraphVertexSchema> getVertexSchemas() {
        return indexProvider.getEntities().stream()
                .flatMap(e -> generateGraphVertexSchema(e).stream())
                .collect(Collectors.toList());
    }

    private List<GraphVertexSchema> generateGraphVertexSchema(Entity e) {
        MappingIndexType type = MappingIndexType.valueOf(e.getPartition().toUpperCase());
        switch (type) {
            case UNIFIED:
                //todo verify correctness
                return  e.getProps().getValues().stream()
                                .map(v -> new GraphVertexSchema.Impl(
                                        e.getType(),
                                        new StaticIndexPartitions(v),
                                        getGraphElementPropertySchemas(e.getType())))
                                .collect(Collectors.toList());
            case NESTED:
                return  e.getProps().getValues().stream()
                                .map(v -> new GraphVertexSchema.Impl(
                                        e.getType(),
                                        new NestedIndexPartitions(v),
                                        getGraphElementPropertySchemas(e.getType())))
                                .collect(Collectors.toList());
            case STATIC:
                return  e.getProps().getValues().stream()
                                .map(v -> new GraphVertexSchema.Impl(
                                        e.getType(),
                                        new StaticIndexPartitions(v),
                                        getGraphElementPropertySchemas(e.getType())))
                                .collect(Collectors.toList());
            case TIME:
                return e.getProps().getValues().stream()
                                .map(v -> new GraphVertexSchema.Impl(
                                        e.getType(),
                                        new TimeBasedIndexPartitions(e.getProps()),
                                        getGraphElementPropertySchemas(e.getType())))
                                .collect(Collectors.toList());
        }
        //default - when other partition type is declared
        String v = e.getProps().getValues().isEmpty() ? e.getType() : e.getProps().getValues().get(0);
        return Collections.singletonList(
                new GraphVertexSchema.Impl(
                        e.getType(),
                        new StaticIndexPartitions(v),
                        getGraphElementPropertySchemas(e.getType())));
    }


    private Optional<List<EPair>> getEdgeSchemaOntologyPairs(String edge) {
        Optional<RelationshipType> relation = accessor.relation(edge);
        return relation.map(RelationshipType::getePairs);
    }

    private List<GraphEdgeSchema> generateGraphEdgeSchema(Relation r, String v, IndexPartitions partitions) {
        Optional<List<EPair>> pairs = getEdgeSchemaOntologyPairs(v);

        if (!pairs.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception", "No edges pairs are found for given relation name " + v));

        List<EPair> pairList = pairs.get();
        validateSchema(pairList);

        return concat(
                pairList.stream().map(p -> constructEdgeSchema(r, v, partitions, p, Direction.OUT)),
                pairList.stream().map(p -> constructEdgeSchema(r, v, partitions, p, Direction.IN)))
                .collect(Collectors.toList());
    }

    private GraphEdgeSchema.Impl constructEdgeSchema(Relation r, String v, IndexPartitions partitions, EPair p, Direction direction) {
        switch (direction) {
            case IN:
                return new GraphEdgeSchema.Impl(
                        v,
                        new GraphElementConstraint.Impl(__.has(T.label, v)),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList(ENTITY_A_ID),
                                Optional.of(p.geteTypeB()),
                                getGraphRedundantPropertySchemas(ENTITY_B, p.geteTypeB(), r))),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList(ENTITY_B_ID),
                                Optional.of(p.geteTypeA()),
                                getGraphRedundantPropertySchemas(ENTITY_A, p.geteTypeA(), r))),
                        direction,
                        Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(DIRECTION, OUT, IN)),
                        Optional.empty(),
                        Optional.of(partitions),
                        Collections.emptyList(),
                        Stream.of(endA).toJavaSet());
            //Also IN case:
            default:
                return new GraphEdgeSchema.Impl(
                        v,
                        new GraphElementConstraint.Impl(__.has(T.label, v)),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList(ENTITY_A_ID),
                                Optional.of(p.geteTypeA()),
                                getGraphRedundantPropertySchemas(ENTITY_A, p.geteTypeA(), r))),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList(ENTITY_B_ID),
                                Optional.of(p.geteTypeB()),
                                getGraphRedundantPropertySchemas(ENTITY_B, p.geteTypeB(), r))),
                        direction,
                        Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(DIRECTION, OUT, IN)),
                        Optional.empty(),
                        Optional.of(partitions),
                        Collections.emptyList(),
                        Stream.of(endA).toJavaSet());

        }

    }

    private void validateSchema(List<EPair> pairList) {
        pairList.forEach(pair -> {
            if (!accessor.entity(pair.geteTypeA()).isPresent() ||
                    !accessor.entity(pair.geteTypeB()).isPresent())
                throw new FuseError.FuseErrorException(new FuseError("Schema generation exception", " Pair containing " + pair.toString() + " was not matched against the current ontology"));
        });
    }

    private List<GraphElementPropertySchema> getGraphElementPropertySchemas(String type) {
        EntityType entityType = accessor.entity$(type);
        List<GraphElementPropertySchema> elementPropertySchemas = new ArrayList<>();
        entityType.getProperties()
                .stream()
                .filter(v -> accessor.pType(v).isPresent())
                .forEach(v -> {
                    switch (accessor.property$(v).getType()) {
                        case TEXT:
                            elementPropertySchemas.add(new GraphElementPropertySchema.Impl(v, accessor.pType$(v),
                                    //todo add all types of possible analyzers - such as ngram ...
                                    Arrays.asList(new GraphElementPropertySchema.ExactIndexingSchema.Impl(v + "." + KEYWORD))));
                            break;
                        default:
                            elementPropertySchemas.add(new GraphElementPropertySchema.Impl(v, accessor.pType$(v)));
                    }
                });

        return elementPropertySchemas;
    }


    private List<GraphRedundantPropertySchema> getGraphRedundantPropertySchemas(String entitySide, String entityType, Relation rel) {
        List<GraphRedundantPropertySchema> redundantPropertySchemas = new ArrayList<>();

        if (!accessor.entity(entityType).get().getMetadata().contains(ID))
            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception", " Entity " + entityType + " not containing " + ID + " metadata property "));

        validateRedundant(entityType, entitySide, rel.getRedundant());
        redundantPropertySchemas.add(new GraphRedundantPropertySchema.Impl(ID, String.format("%s.%s", entitySide, ID), "string"));
        //add all RedundantProperty according to schema
        validateRedundant(entityType, entitySide, rel.getRedundant());
        rel.getRedundant()
                .stream()
                .filter(r -> r.getSide().contains(entitySide))
                .forEach(r -> {
                    redundantPropertySchemas.add(new GraphRedundantPropertySchema.Impl(r.getName(), String.format("%s.%s", entitySide, r.getName()), r.getType()));
                });
        return redundantPropertySchemas;
    }

    private void validateRedundant(String entityType, String entitySide, List<Redundant> redundant) {
        redundant.stream()
                .filter(r -> r.getSide().contains(entitySide))
                .forEach(r -> {
                    if (!accessor.entity(entityType).get().getProperties().contains(r.getName()))
                        throw new FuseError.FuseErrorException(new FuseError("Schema generation exception", " Entity " + entityType + " not containing " + r.getName() + " property (as redundant ) "));
                });
    }

    /**
     * new GraphEdgeSchema.Impl(
     * "fire",
     * new GraphElementConstraint.Impl(__.has(T.label, "fire")),
     * Optional.of(new GraphEdgeSchema.End.Impl(
     * Collections.singletonList("entityA.id"),
     * Optional.of("Dragon"),
     * Arrays.asList(
     * new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
     * new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
     * ))),
     * Optional.of(new GraphEdgeSchema.End.Impl(
     * Collections.singletonList("entityB.id"),
     * Optional.of("Dragon"),
     * Arrays.asList(
     * new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
     * new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
     * ))),
     * Direction.OUT,
     * Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
     * Optional.empty(),
     * Optional.of(new StaticIndexPartitions(Collections.singletonList(FIRE.getName().toLowerCase()))),
     * Collections.emptyList(),
     * Stream.of(endA).toJavaSet())
     */

    public static class TimeBasedIndexPartitions implements TimeSeriesIndexPartitions {
        private Props props;
        private SimpleDateFormat dateFormat;

        TimeBasedIndexPartitions(Props props) {
            this.props = props;
            this.dateFormat = new SimpleDateFormat(getDateFormat());
        }


        @Override
        public String getDateFormat() {
            return props.getDateFormat();
        }

        @Override
        public String getIndexPrefix() {
            return props.getPrefix();
        }

        @Override
        public String getIndexFormat() {
            return props.getIndexFormat();
        }

        @Override
        public String getTimeField() {
            return props.getPartitionField();
        }

        @Override
        public String getIndexName(Date date) {
            String format = String.format(getIndexFormat(), dateFormat.format(date));
            List<String> indices = Stream.ofAll(getPartitions())
                    .flatMap(Partition::getIndices)
                    .filter(index -> index.equals(format))
                    .toJavaList();

            return indices.isEmpty() ? null : indices.get(0);
        }

        @Override
        public Optional<String> getPartitionField() {
            return Optional.of(getTimeField());
        }

        @Override
        public Iterable<Partition> getPartitions() {
            return Collections.singletonList(() -> Stream.ofAll(props.getValues())
                    .map(p -> String.format(getIndexFormat(), p))
                    .distinct().sorted()
                    .toJavaList());
        }
    }


}
