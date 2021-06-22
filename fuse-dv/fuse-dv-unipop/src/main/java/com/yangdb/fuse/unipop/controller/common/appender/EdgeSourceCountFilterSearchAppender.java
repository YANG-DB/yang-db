package com.yangdb.fuse.unipop.controller.common.appender;

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
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.controller.search.translation.CountFilterQueryTranslator;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.process.predicate.CountFilterP;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by roman.margolis on 18/10/2017.
 *
 * todo - add as a separate step before the actual fetching of the documents due to the next missing DSL...
 * https://discuss.elastic.co/t/how-to-get-documents-in-elasticsearch-based-on-aggregation-output-values/182109
 */
public class EdgeSourceCountFilterSearchAppender implements SearchAppender<VertexControllerContext> {
    private CountFilterQueryTranslator translator = new CountFilterQueryTranslator();

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

        List<HasContainer> hasContainers = StreamSupport.stream(context.getSelectPHasContainers().spliterator(), false)
                .filter(p -> CountFilterP.class.isAssignableFrom(p.getPredicate().getClass()))
                .collect(Collectors.toList());

        //add aggregation predicate
        hasContainers.stream()
                .filter(p -> translator.test("key", p.getPredicate()))
                .forEach(p -> translator.translate(searchBuilder.getQueryBuilder(), searchBuilder.getAggregationBuilder(), "key", p.getPredicate()));

        return true;
    }
    //endregion
}

