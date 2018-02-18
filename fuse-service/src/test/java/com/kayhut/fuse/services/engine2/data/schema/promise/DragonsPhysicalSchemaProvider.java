package com.kayhut.fuse.services.engine2.data.schema.promise;

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
public class DragonsPhysicalSchemaProvider implements GraphElementSchemaProvider {
    //region GraphElementSchemaProvider Implementation
    @Override
    public Iterable<GraphVertexSchema> getVertexSchemas(String label) {
        if (!Stream.ofAll(getVertexLabels()).contains(label)) {
            return Collections.emptyList();
        }

        switch (label) {
            case "Person":
            case "Dragon":
                return Collections.singletonList(new GraphVertexSchema.Impl(label, new StaticIndexPartitions(label.toLowerCase())));

            case "Kingdom":
            case "Horse":
            case "Guild":
                return Collections.singletonList(new GraphVertexSchema.Impl(label, new StaticIndexPartitions()));
        }

        return Collections.emptyList();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
        if (!Stream.ofAll(getEdgeLabels()).contains(label)) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new GraphEdgeSchema.Impl(
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
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label) {
        return null;
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label) {
        return null;
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB) {
        return null;
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
        return Collections.singletonList(FIRE.getName());
    }
    //endregion
}
