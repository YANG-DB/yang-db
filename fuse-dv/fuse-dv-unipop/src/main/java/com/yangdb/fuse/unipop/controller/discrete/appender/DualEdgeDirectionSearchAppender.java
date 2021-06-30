package com.yangdb.fuse.unipop.controller.discrete.appender;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.yangdb.fuse.unipop.controller.common.context.VertexControllerContext;
import com.yangdb.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.yangdb.fuse.unipop.controller.search.AggregationBuilder;
import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Set;

import static com.yangdb.fuse.unipop.controller.common.appender.EdgeUtils.getLabel;

/**
 * Created by roman.margolis on 22/01/2018.
 */
public class DualEdgeDirectionSearchAppender extends SearchQueryAppenderBase<VertexControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, VertexControllerContext context) {
        Set<String> labels = context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        //currently assuming a single edge label
        String edgeLabel = Stream.ofAll(labels).get(0);

        //currently assuming a single vertex label in bulk
        String contextVertexLabel = getLabel(context,"?");


        Iterable<GraphEdgeSchema> edgeSchemas = context.getSchemaProvider().getEdgeSchemas(contextVertexLabel, context.getDirection(), edgeLabel);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);
        if (!edgeSchema.getDirectionSchema().isPresent()) {
            return false;
        }

        if (context.getDirection().equals(Direction.BOTH)) {
            return false;
        }

        Traversal directionConstraint = __.start();
        switch (context.getDirection()) {
            case OUT:
                    directionConstraint = __.has(edgeSchema.getDirectionSchema().get().getField(), edgeSchema.getDirectionSchema().get().getOutValue());
                break;
            case IN:
                    directionConstraint = __.has(edgeSchema.getDirectionSchema().get().getField(), edgeSchema.getDirectionSchema().get().getInValue());
                break;
        }

        QueryBuilder builder = queryBuilder.seekRoot().query().bool().filter().bool().must();
        AggregationBuilder aggBuilder = aggregationBuilder.seekRoot();
        TraversalQueryTranslator traversalQueryTranslator =
                new TraversalQueryTranslator(builder, aggBuilder, false);
        traversalQueryTranslator.visit(directionConstraint);
        return true;
    }
    //endregion
}
