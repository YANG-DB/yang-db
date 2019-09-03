package com.yangdb.fuse.executor.ontology.schema;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.ontology.EPair;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.Relation;
import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endA;

public class GraphElementSchemaProviderJsonFactory implements GraphElementSchemaProviderFactory {

    public static final String ENTITY_A_ID = "entityA.id";
    public static final String ID = "id";
    public static final String ENTITY_B_ID = "entityB.id";
    public static final String DIRECTION = "direction";
    public static final String OUT = "out";
    public static final String IN = "in";
    private IndexProvider indexProvider;
    private Ontology ontology;
    private Ontology.Accessor accessor = new Ontology.Accessor(ontology);

    @Inject
    public GraphElementSchemaProviderJsonFactory(IndexProvider indexProvider, Ontology ontology) {
        this.indexProvider = indexProvider;
        this.ontology = ontology;
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
        switch (r.getPartition()) {
            case "static":
                return
                        r.getProps().getValues().stream()
                                .flatMap(v -> generateGraphEdgeSchema(r, v).stream())
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
                                .map(v -> createGraphVertexSchema(e, v))
                                .collect(Collectors.toList());
            case "time":
                //todo
                break;
        }
        return Collections.singletonList(createGraphVertexSchema(e, e.getProps().getValues().isEmpty() ? e.getType() : e.getProps().getValues().get(0)));
    }

    private GraphVertexSchema.Impl createGraphVertexSchema(Entity e, String v) {
        return new GraphVertexSchema.Impl(e.getType(), new StaticIndexPartitions(v));
    }

    private Optional<List<EPair>> getEdgeSchemaOntologyPairs(String edge) {
        Optional<RelationshipType> relation = accessor.relation(edge);
        return relation.map(RelationshipType::getePairs);
    }

    private List<GraphEdgeSchema.Impl> generateGraphEdgeSchema(Relation r, String v) {
        Optional<List<EPair>> pairs = getEdgeSchemaOntologyPairs(v);
        List<EPair> pairList = pairs.get();
        return pairList.stream().map(p -> new GraphEdgeSchema.Impl(
                v,
                new GraphElementConstraint.Impl(__.has(T.label, v)),
                Optional.of(new GraphEdgeSchema.End.Impl(
                        Collections.singletonList(ENTITY_A_ID),
                        Optional.of(p.geteTypeA()),
                        getGraphRedundantPropertySchemas(ENTITY_A_ID, p.geteTypeA()))),
                Optional.of(new GraphEdgeSchema.End.Impl(
                        Collections.singletonList(ENTITY_B_ID),
                        Optional.of(p.geteTypeB()),
                        getGraphRedundantPropertySchemas(ENTITY_B_ID, p.geteTypeB()))),
                Direction.OUT,
                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(DIRECTION, OUT, IN)),
                Optional.empty(),
                Optional.of(new StaticIndexPartitions(Collections.singletonList(v))),
                Collections.emptyList(),
                Stream.of(endA).toJavaSet()))
                .collect(Collectors.toList());
    }

    private List<GraphRedundantPropertySchema> getGraphRedundantPropertySchemas(String idName, String typeName) {
        List<GraphRedundantPropertySchema> redundantPropertySchemas = new ArrayList<>();
        redundantPropertySchemas.add(new GraphRedundantPropertySchema.Impl(ID, ENTITY_B_ID, "string"));
        //todo add all RedundantProperty according to schema
        return redundantPropertySchemas;
    }

    /**
     *                         new GraphEdgeSchema.Impl(
     *                                 "fire",
     *                                 new GraphElementConstraint.Impl(__.has(T.label, "fire")),
     *                                 Optional.of(new GraphEdgeSchema.End.Impl(
     *                                         Collections.singletonList("entityA.id"),
     *                                         Optional.of("Dragon"),
     *                                         Arrays.asList(
     *                                                 new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
     *                                                 new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
     *                                         ))),
     *                                 Optional.of(new GraphEdgeSchema.End.Impl(
     *                                         Collections.singletonList("entityB.id"),
     *                                         Optional.of("Dragon"),
     *                                         Arrays.asList(
     *                                                 new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
     *                                                 new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
     *                                         ))),
     *                                 Direction.OUT,
     *                                 Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
     *                                 Optional.empty(),
     *                                 Optional.of(new StaticIndexPartitions(Collections.singletonList(FIRE.getName().toLowerCase()))),
     *                                 Collections.emptyList(),
     *                                 Stream.of(endA).toJavaSet())
     */

}
