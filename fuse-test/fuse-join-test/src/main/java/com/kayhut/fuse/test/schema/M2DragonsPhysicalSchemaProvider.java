package com.kayhut.fuse.test.schema;

import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.*;

import static com.kayhut.fuse.model.OntologyTestUtils.*;

/**
 * Created by roman.margolis on 28/09/2017.
 */
public class M2DragonsPhysicalSchemaProvider implements GraphElementSchemaProvider {
    public M2DragonsPhysicalSchemaProvider() {

        this.schemas = new ArrayList<>();
        this.schemas.add(new GraphEdgeSchema.Impl(
                "fire",
                Optional.of(new GraphEdgeSchema.End.Impl(
                        Collections.singletonList("entityA.id"),
                        Optional.of("Dragon"),
                        Arrays.asList(
                                new GraphRedundantPropertySchema.Impl("id", "entityA.id", "string"),
                                new GraphRedundantPropertySchema.Impl("type", "entityA.type", "string")
                        ))),
                Optional.of(new GraphEdgeSchema.End.Impl(
                        Collections.singletonList("entityB.id"),
                        Optional.of("Dragon"),
                        Arrays.asList(
                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                        ))),
                Optional.of(new GraphEdgeSchema.Direction.Impl("direction", "out", "in")),
                new StaticIndexPartitions(Arrays.asList(
                        FIRE.getName().toLowerCase() + "20170511",
                        FIRE.getName().toLowerCase() + "20170512",
                        FIRE.getName().toLowerCase() + "20170513"))));

        this.schemas.add(new GraphEdgeSchema.Impl(
                "originatedIn",
                new GraphElementConstraint.Impl(__.has(T.label, "originatedIn")),
                Optional.of(new GraphEdgeSchema.End.Impl(
                        Collections.singletonList("entityA.id"),
                        Optional.of("Dragon"),
                        Arrays.asList(
                                new GraphRedundantPropertySchema.Impl("id", "entityA.id", "string"),
                                new GraphRedundantPropertySchema.Impl("type", "entityA.type", "string")
                        ))),
                Optional.of(new GraphEdgeSchema.End.Impl(
                        Collections.singletonList("entityB.id"),
                        Optional.of("Kingdom"),
                        Arrays.asList(
                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                        ))),
                //Optional.of(new GraphEdgeSchema.Direction.Impl("direction", "OUT", "IN")),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new StaticIndexPartitions(Arrays.asList("originated_in"))),
                Collections.emptyList(),
                Stream.of(GraphEdgeSchema.Application.source, GraphEdgeSchema.Application.destination).toJavaSet()));

       /* this.schemas.add(new GraphEdgeSchema.Impl(
                "originatedIn",
                new GraphElementConstraint.Impl(__.has(T.label, "originatedIn")),
                Optional.of(new GraphEdgeSchema.End.Impl(
                        Collections.singletonList("entityA.id"),
                        Optional.of("Kingdom"),
                        Arrays.asList(
                                new GraphRedundantPropertySchema.Impl("id", "entityA.id", "string"),
                                new GraphRedundantPropertySchema.Impl("type", "entityA.type", "string")
                        ))),
                Optional.of(new GraphEdgeSchema.End.Impl(
                        Collections.singletonList("entityB.id"),
                        Optional.of("Dragon"),
                        Arrays.asList(
                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", "string"),
                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", "string")
                        ))),
                //Optional.of(new GraphEdgeSchema.Direction.Impl("direction", "OUT", "IN")),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new StaticIndexPartitions(Arrays.asList("originated_in"))),
                Collections.emptyList(),
                Stream.of(GraphEdgeSchema.Application.source).toJavaSet()));
*/
    }

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
        return Stream.ofAll(this.schemas).find(s -> s.getLabel().equals(label)).toJavaOptional();
    }

    @Override
    public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
        return Stream.ofAll(this.schemas).filter(s -> s.getLabel().equals(label)).toJavaList();
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

    private List<GraphEdgeSchema> schemas;
}
