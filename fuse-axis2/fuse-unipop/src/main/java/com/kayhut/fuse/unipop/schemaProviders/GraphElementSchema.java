package com.kayhut.fuse.unipop.schemaProviders;


import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Created by r on 1/16/2015.
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
                    new GraphElementConstraint.Impl(__.has(T.label, label)),
                    Optional.of(routing),
                    Optional.empty(),
                    Collections.emptyList());
        }

        public Impl(String label, IndexPartitions indexPartitions) {
            this(label,
                    new GraphElementConstraint.Impl(__.has(T.label, label)),
                    Optional.empty(),
                    Optional.of(indexPartitions),
                    Collections.emptyList());
        }

        public Impl(String label,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions) {
            this(label, new GraphElementConstraint.Impl(__.has(T.label, label)), routing, indexPartitions, Collections.emptyList());
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
