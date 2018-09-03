package com.kayhut.fuse.services.engine2.data.schema.discrete;

import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.test.data.DragonsOntology.POWER;

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
                return Collections.singletonList(new GraphVertexSchema.Impl(
                        label,
                        Optional.empty(),
                        Optional.of(new IndexPartitions.Impl("_id",
                                new IndexPartitions.Partition.Range.Impl<>("p001", "p005", "person1"),
                                new IndexPartitions.Partition.Range.Impl<>("p005", "p010", "person2")))
                ));
            case "Dragon":
                return Collections.singletonList(new GraphVertexSchema.Impl(
                        label,
                        Optional.of(new GraphElementRouting.Impl(
                                new GraphElementPropertySchema.Impl("personId", "string")
                        )),
                        Optional.of(new IndexPartitions.Impl("personId",
                                new IndexPartitions.Partition.Range.Impl<>("p001", "p005", "dragon1"),
                                new IndexPartitions.Partition.Range.Impl<>("p005", "p010", "dragon2")))
                ));

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

        switch (label) {
            case "own":
                return Collections.singletonList(new GraphEdgeSchema.Impl(
                        "own",
                        new GraphElementConstraint.Impl(__.has(T.label, "Dragon")),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList("personId"),
                                Optional.of("Person"),
                                Collections.emptyList(),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("_id", "string")
                                )),
                                Optional.of(new IndexPartitions.Impl("_id",
                                        new IndexPartitions.Partition.Range.Impl<>("p001", "p005", "dragon1"),
                                        new IndexPartitions.Partition.Range.Impl<>("p005", "p010", "dragon2")))
                        )),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList("_id"),
                                Optional.of("Dragon"),
                                Arrays.asList(
                                        new GraphRedundantPropertySchema.Impl(NAME.name, NAME.name, NAME.type),
                                        new GraphRedundantPropertySchema.Impl(BIRTH_DATE.name, BIRTH_DATE.name, BIRTH_DATE.type),
                                        new GraphRedundantPropertySchema.Impl(POWER.name, POWER.name, POWER.type),
                                        new GraphRedundantPropertySchema.Impl(GENDER.name, GENDER.name, GENDER.type),
                                        new GraphRedundantPropertySchema.Impl(COLOR.name, COLOR.name, COLOR.type)
                                ),
                                Optional.of(new GraphElementRouting.Impl(
                                        new GraphElementPropertySchema.Impl("personId", "string")
                                )),
                                Optional.of(new IndexPartitions.Impl("personId",
                                        new IndexPartitions.Partition.Range.Impl<>("p001", "p005", "dragon1"),
                                        new IndexPartitions.Partition.Range.Impl<>("p005", "p010", "dragon2")))
                        )),
                        Direction.OUT,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Collections.emptyList()
                ));

            default:
                return Collections.singletonList(new GraphEdgeSchema.Impl(
                        label,
                        new StaticIndexPartitions()
                ));
        }
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB) {
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
        return Collections.singletonList(OWN.getName());
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return Collections.emptyList();
    }
    //endregion
}
