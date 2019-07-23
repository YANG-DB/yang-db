package com.yangdb.fuse.unipop.schemaProviders;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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


import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import com.yangdb.fuse.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Created by roman on 1/16/2015.
 */
public interface GraphElementSchema {
    Class getSchemaElementType();

    String getLabel();

    GraphElementConstraint getConstraint();

    Optional<GraphElementRouting> getRouting();

    Optional<IndexPartitions> getIndexPartitions();

    Iterable<GraphElementPropertySchema> getProperties();

    Optional<GraphElementPropertySchema> getProperty(String name);

    abstract class Impl implements GraphElementSchema {
        //region Constructors
        public Impl(String label, GraphElementRouting routing) {
            this(label,
                    new GraphElementConstraint.Impl(__.start().has(T.label, label)),
                    Optional.of(routing),
                    Optional.empty(),
                    Collections.emptyList());
        }

        public Impl(String label, IndexPartitions indexPartitions) {
            this(label,
                    new GraphElementConstraint.Impl(__.start().has(T.label, label)),
                    Optional.empty(),
                    Optional.of(indexPartitions),
                    Collections.emptyList());
        }

        public Impl(String label,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions) {
            this(label, new GraphElementConstraint.Impl(__.start().has(T.label, label)), routing, indexPartitions, Collections.emptyList());
        }

        public Impl(String label,
                    GraphElementConstraint constraint,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions,
                    Iterable<GraphElementPropertySchema> properties) {
            this.label = label;
            this.constraint = constraint;
            this.routing = routing;
            this.indexPartitions = indexPartitions;
            this.properties = Stream.ofAll(properties).toJavaMap(property -> new Tuple2<>(property.getName(), property));
        }
        //endregion

        //region GraphElementSchema Implementation
        @Override
        public String getLabel() {
            return this.label;
        }

        @Override
        public Optional<GraphElementRouting> getRouting() {
            return this.routing;
        }

        @Override
        public Optional<IndexPartitions> getIndexPartitions() {
            return this.indexPartitions;
        }

        @Override
        public Iterable<GraphElementPropertySchema> getProperties() {
            return this.properties.values();
        }

        @Override
        public Optional<GraphElementPropertySchema> getProperty(String name) {
            return Optional.ofNullable(this.properties.get(name));
        }

        @Override
        public GraphElementConstraint getConstraint() {
            return this.constraint;
        }
        //endregion

        //region Fields
        private String label;
        private GraphElementConstraint constraint;
        private Optional<GraphElementRouting> routing;
        private Optional<IndexPartitions> indexPartitions;
        private Map<String, GraphElementPropertySchema> properties;
        //endregion
    }
}
