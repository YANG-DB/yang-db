package com.kayhut.fuse.services.engine2.data.schema;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;

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
    public Optional<GraphVertexSchema> getVertexSchema(String label) {
        if (!Stream.ofAll(getVertexLabels()).contains(label)) {
            return Optional.empty();
        }

        switch (label) {
            case "Person":
            case "Dragon":
                return Optional.of(new GraphVertexSchema.Impl(label, new StaticIndexPartitions(label.toLowerCase())));

            case "Kingdom":
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

        return Optional.of(new GraphEdgeSchema.Impl(
                label,
                Optional.of(new GraphEdgeSchema.End.Impl(
                        "entityA.id",
                        Optional.empty(),
                        Arrays.asList(
                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                        ))),
                Optional.of(new GraphEdgeSchema.End.Impl(
                        "entityB.id",
                        Optional.empty(),
                        Arrays.asList(
                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                        ))),
                Optional.of(new GraphEdgeSchema.Direction.Impl("direction", "out", "in")),
                new StaticIndexPartitions(Arrays.asList(
                        FIRE.getName().toLowerCase() + "20170511",
                        FIRE.getName().toLowerCase() + "20170512",
                        FIRE.getName().toLowerCase() + "20170513"))));
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
        return Collections.singletonList(FIRE.getName());
    }
    //endregion
}
