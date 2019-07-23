package com.yangdb.fuse.services.engine2.data.schema.discrete;

import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.yangdb.fuse.model.OntologyTestUtils.*;
import static com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endA;

/**
 * Created by roman.margolis on 28/09/2017.
 */
public class M2DragonsPhysicalSchemaProvider extends GraphElementSchemaProvider.Impl {
    public M2DragonsPhysicalSchemaProvider() {
        super(
                Arrays.asList(
                        new GraphVertexSchema.Impl("Person", new StaticIndexPartitions("person")),
                        new GraphVertexSchema.Impl("Dragon", new StaticIndexPartitions("dragon")),
                        new GraphVertexSchema.Impl("Kingdom", new StaticIndexPartitions("kingdom")),
                        new GraphVertexSchema.Impl("Horse", new StaticIndexPartitions()),
                        new GraphVertexSchema.Impl("Guild", new StaticIndexPartitions())
                ),
                Arrays.asList(
                        new GraphEdgeSchema.Impl(
                                "fire",
                                new GraphElementConstraint.Impl(__.has(T.label, "fire")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityA.id"),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                                        ))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityB.id"),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                                        ))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList(
                                        FIRE.getName().toLowerCase() + "20170511",
                                        FIRE.getName().toLowerCase() + "20170512",
                                        FIRE.getName().toLowerCase() + "20170513"))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "fire",
                                new GraphElementConstraint.Impl(__.has(T.label, "fire")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityA.id"),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                                        ))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityB.id"),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                                        ))),
                                Direction.IN,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList(
                                        FIRE.getName().toLowerCase() + "20170511",
                                        FIRE.getName().toLowerCase() + "20170512",
                                        FIRE.getName().toLowerCase() + "20170513"))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "originatedIn",
                                new GraphElementConstraint.Impl(__.has(T.label, "originatedIn")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityA.id"),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                                        ))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityB.id"),
                                        Optional.of("Kingdom"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                                        ))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "OUT", "IN")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList("originated_in"))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "originatedIn",
                                new GraphElementConstraint.Impl(__.has(T.label, "originatedIn")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityA.id"),
                                        Optional.of("Kingdom"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                                        ))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityB.id"),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                                        ))),
                                Direction.IN,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "OUT", "IN")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList("originated_in"))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet())
                        )
        );
    }
}
