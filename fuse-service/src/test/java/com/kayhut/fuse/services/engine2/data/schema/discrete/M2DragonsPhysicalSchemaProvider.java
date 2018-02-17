package com.kayhut.fuse.services.engine2.data.schema.discrete;

import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.*;

/**
 * Created by roman.margolis on 28/09/2017.
 */
public class M2DragonsPhysicalSchemaProvider implements GraphElementSchemaProvider {
    @Override
    public Optional<GraphVertexSchema> getVertexSchema(String label) {
        if (!Stream.ofAll(getVertexLabels()).contains(label)) {
            return Optional.empty();
        }

        switch (label) {
            case "Person":
            case "Dragon":
            case "Kingdom":
                return Optional.of(new GraphVertexSchema.Impl(label, new StaticIndexPartitions(label.toLowerCase())));

            case "Horse":
            case "Guild":
                return Optional.of(new GraphVertexSchema.Impl(label, new StaticIndexPartitions()));
        }

        return Optional.empty();
    }

    @Override
    public Optional<GraphEdgeSchema> getEdgeSchema(String label) {
        if (!Stream.ofAll(getEdgeLabels()).contains(label)) {
            return Optional.empty();
        }
        switch(label) {
            case "fire":
                return Optional.of(new GraphEdgeSchema.Impl(
                        label,
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
                        new StaticIndexPartitions(Arrays.asList(
                                FIRE.getName().toLowerCase() + "20170511",
                                FIRE.getName().toLowerCase() + "20170512",
                                FIRE.getName().toLowerCase() + "20170513"))));
            case "originatedIn":
                return Optional.of(new GraphEdgeSchema.Impl(
                        label,
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
                        new StaticIndexPartitions(Arrays.asList("originated_in"))));
        }
        return Optional.empty();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
        Optional<GraphEdgeSchema> graphEdgeSchema = getEdgeSchema(label);
        if (graphEdgeSchema.isPresent()) {
            return Collections.singletonList(graphEdgeSchema.get());
        }

        return Collections.emptyList();
    }

    @Override
    public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
        return Optional.empty();
    }

    @Override
    public Iterable<String> getVertexLabels() {
        return Arrays.asList(
                PERSON.name,
                DRAGON.name,
                KINGDOM.name,
                HORSE.name,
                GUILD.name);
    }

    @Override
    public Iterable<String> getEdgeLabels() {
        return Arrays.asList(FIRE.getName(), ORIGINATED_IN.getName());
    }

}
