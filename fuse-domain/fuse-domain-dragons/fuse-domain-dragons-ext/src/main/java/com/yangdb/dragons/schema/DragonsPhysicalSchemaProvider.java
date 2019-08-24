package com.yangdb.dragons.schema;

/*-
 * #%L
 * fuse-domain-dragons-ext
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.yangdb.dragons.schema.DragonsOntologyUtils.*;

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
                                Optional.of(new StaticIndexPartitions("dragon"))
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
                                Optional.of(new StaticIndexPartitions("dragon"))
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
