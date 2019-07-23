package com.yangdb.fuse.unipop.controller.common.appender;

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

import com.yangdb.fuse.unipop.controller.common.context.VertexControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.controller.utils.ElementUtil;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Created by roman.margolis on 18/10/2017.
 */
public class EdgeRoutingSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Set<String> labels = context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        //currently assuming a single edge label
        String edgeLabel = Stream.ofAll(labels).get(0);

        //currently assuming a single vertex label in bulk
        String contextVertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();


        Iterable<GraphEdgeSchema> edgeSchemas = context.getSchemaProvider().getEdgeSchemas(contextVertexLabel, context.getDirection(), edgeLabel);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);
        GraphEdgeSchema.End endSchema = edgeSchema.getEndA().get();

        if (endSchema.getRouting().isPresent()) {
            boolean isRoutingFieldFullyAvailable =
                    Stream.ofAll(context.getBulkVertices())
                            .map(vertex -> ElementUtil.<String>value(vertex,
                                    translateRoutingPropertyName(endSchema.getRouting().get().getRoutingProperty().getName())))
                            .filter(value -> !value.isPresent())
                            .size() == 0;

            Set<String> routingValues = Collections.emptySet();
            if (isRoutingFieldFullyAvailable) {
                routingValues =
                        Stream.ofAll(context.getBulkVertices())
                                .map(vertex -> ElementUtil.<String>value(vertex,
                                        translateRoutingPropertyName(endSchema.getRouting().get().getRoutingProperty().getName())))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toJavaSet();
            }

            searchBuilder.getRouting().addAll(routingValues);
            return routingValues.size() > 0;
        }

        return false;
    }
    //endregion

    //region Private Methods
    private String translateRoutingPropertyName(String name) {
        return name.equals("_id") ? T.id.getAccessor() : name;
    }
    //endregion
}
