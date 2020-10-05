package com.yangdb.fuse.services.engine2.data.schema.promise;

import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.yangdb.fuse.model.OntologyTestUtils.*;

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
                                Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
                                Optional.of("Dragon"),
                                Arrays.asList(
                                        new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                        new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                ))),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID),
                                Optional.of("Dragon"),
                                Arrays.asList(
                                        new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                        new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
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
