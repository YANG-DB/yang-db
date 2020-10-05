package com.yangdb.test.schema;

/*-
 * #%L
 * fuse-domain-dragons-test
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema.Application.endA;
import static com.yangdb.test.data.DragonsOntology.FIRE;

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
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "OUT", "IN")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList(
                                        FIRE.getName().toLowerCase()))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "fire",
                                new GraphElementConstraint.Impl(__.has(T.label, "fire")),
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
                                Direction.IN,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "OUT", "IN")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList(
                                        FIRE.getName().toLowerCase()))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "originatedIn",
                                new GraphElementConstraint.Impl(__.has(T.label, "originatedIn")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                        ))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID),
                                        Optional.of("Kingdom"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                        ))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "OUT", "IN")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList("originated_in"))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                "originatedIn",
                                new GraphElementConstraint.Impl(__.has(T.label, "originatedIn")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
                                        Optional.of("Kingdom"),
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
                                Direction.IN,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "OUT", "IN")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList("originated_in"))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet())
                )
        );
    }
}
