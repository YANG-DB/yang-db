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
public class DragonsPhysicalSchemaProvider extends GraphElementSchemaProvider.Impl {
    public DragonsPhysicalSchemaProvider() {
        super(
                Arrays.asList(
                        new GraphVertexSchema.Impl("Person", new StaticIndexPartitions("person")),
                        new GraphVertexSchema.Impl("Dragon", new StaticIndexPartitions("dragon")),
                        new GraphVertexSchema.Impl("Kingdom", new StaticIndexPartitions()),
                        new GraphVertexSchema.Impl("Horse", new StaticIndexPartitions()),
                        new GraphVertexSchema.Impl("Guild", new StaticIndexPartitions())
                ),
                Collections.singletonList(new GraphEdgeSchema.Impl(
                        "fire",
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
                                FIRE.getName().toLowerCase() + "20170513"))))
        );
    }
    //endregion
}
