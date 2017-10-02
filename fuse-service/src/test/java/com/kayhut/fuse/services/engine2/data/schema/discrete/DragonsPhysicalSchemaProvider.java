package com.kayhut.fuse.services.engine2.data.schema.discrete;

import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;

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
    public Optional<GraphVertexSchema> getVertexSchema(String label) {
        if (!Stream.ofAll(getVertexLabels()).contains(label)) {
            return Optional.empty();
        }

        switch (label) {
            case "Person":
                return Optional.of(new GraphVertexSchema.Impl(
                        label,
                        Optional.empty(),
                        Optional.of(new IndexPartitions.Impl("_id",
                                new IndexPartitions.Partition.Range.Impl<>("p001", "p005", "person1"),
                                new IndexPartitions.Partition.Range.Impl<>("p005", "p010", "person2")))
                ));
            case "Dragon":
                return Optional.of(new GraphVertexSchema.Impl(
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
                return Optional.of(new GraphVertexSchema.Impl(label, new StaticIndexPartitions()));
        }

        return Optional.empty();
    }

    @Override
    public Optional<GraphEdgeSchema> getEdgeSchema(String label) {
        if (!Stream.ofAll(getEdgeLabels()).contains(label)) {
            return Optional.empty();
        }

        switch (label) {
            case "own":
                return Optional.of(new GraphEdgeSchema.Impl(
                        "own",
                        "Dragon",
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                "personId",
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
                                "_id",
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
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Collections.emptyList()
                ));

            default: return Optional.of(new GraphEdgeSchema.Impl(
                    label,
                    new StaticIndexPartitions()
            ));
        }
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
        return Collections.singletonList(OWN.getName());
    }
    //endregion
}
