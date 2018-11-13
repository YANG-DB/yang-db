package com.kayhut.fuse.unipop.controller.promise.appender;

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

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Created by Elad on 4/26/2017.
 */
public class StartVerticesSearchAppender implements SearchAppender<VertexControllerContext> {

    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {

        Traversal traversal = buildStartVerticesConstraint(context.getBulkVertices());

        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query().bool().filter().bool().must();

        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder, false);

        traversalQueryTranslator.visit(traversal);

        return true;

    }

    private Traversal buildStartVerticesConstraint(Iterable<Vertex> vertices) {
        return __.has(GlobalConstants.EdgeSchema.SOURCE_ID, P.within(Stream.ofAll(vertices).map(vertex -> vertex.id()).toJavaList()));
    }

}
