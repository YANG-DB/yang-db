package com.kayhut.fuse.unipop.controller.common.appender;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;
import com.kayhut.fuse.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.*;

/**
 * Created by roman.margolis on 18/09/2017.
 */
public class FilterSourceRoutingSearchAppender implements SearchAppender<CompositeControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, CompositeControllerContext context) {
        Set<String> labels = getContextRelevantLabels(context);

        // not supporintg currently routing fields for edges
        Set<String> routingFields = Stream.ofAll(labels)
                .flatMap(label -> context.getElementType().equals(ElementType.vertex) ?
                        context.getSchemaProvider().getVertexSchemas(label) :
                        Collections.emptyList())
                .map(GraphElementSchema::getRouting)
                .filter(Optional::isPresent)
                .map(routing -> routing.get().getRoutingProperty().getName())
                .toJavaSet();

        // gather additional routing fields of vertices for edges
        if (context.getElementType().equals(ElementType.vertex)) {
            routingFields.addAll(
                    Stream.ofAll(context.getSchemaProvider().getEdgeLabels())
                    .flatMap(label -> context.getSchemaProvider().getEdgeSchemas(label))
                    .flatMap(edgeSchema -> Arrays.asList(edgeSchema.getEndA(), edgeSchema.getEndB()))
                    .filter(Optional::isPresent)
                    .filter(endSchema -> labels.contains(endSchema.get().getLabel().get()))
                    .filter(endSchema -> endSchema.get().getRouting().isPresent())
                    .map(endSchema -> endSchema.get().getRouting().get().getRoutingProperty().getName())
                    .toJavaSet());
        }

        searchBuilder.getIncludeSourceFields().addAll(routingFields);

        return routingFields.size() > 0;
    }
    //endregion

    //region Private Methods
    private Set<String> getContextRelevantLabels(CompositeControllerContext context) {
        if (context.getVertexControllerContext().isPresent()) {
            return getVertexContextRelevantLabels(context);
        }

        return getElementContextRelevantLabels(context);
    }

    private Set<String> getElementContextRelevantLabels(ElementControllerContext context) {
        Set<String> labels = Collections.emptySet();
        if (context.getConstraint().isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());
        }

        if (labels.isEmpty()) {
            labels = Stream.ofAll(context.getElementType().equals(ElementType.vertex) ?
                    context.getSchemaProvider().getVertexLabels() :
                    context.getSchemaProvider().getEdgeLabels()).toJavaSet();
        }

        return labels;
    }

    private Set<String> getVertexContextRelevantLabels(VertexControllerContext context) {
        // currently assuming homogeneous bulk
        return Stream.ofAll(context.getBulkVertices())
                .take(1)
                .map(Element::label)
                .toJavaSet();
    }
    //endregion
}
