package org.unipop.query.search;

/*-
 * #%L
 * DeferredVertexQuery.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.javatuples.Pair;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;
import org.unipop.schema.reference.DeferredVertex;
import org.unipop.query.StepDescriptor;
import org.unipop.query.UniQuery;
import org.unipop.query.controller.UniQueryController;

import java.util.List;
import java.util.Set;

public class DeferredVertexQuery extends SearchQuery<Vertex> {
    private List<DeferredVertex> vertices;

    public DeferredVertexQuery(List<DeferredVertex> vertices, Set<String> propertyKeys, List<Pair<String, Order>> orders, StepDescriptor stepDescriptor) {
        super(Vertex.class, PredicatesHolderFactory.empty(), -1, propertyKeys, orders, stepDescriptor);
        this.vertices = vertices;
    }

    public List<DeferredVertex> getVertices() {
        return this.vertices;
    }

    public interface DeferredVertexController extends UniQueryController {
        void fetchProperties(DeferredVertexQuery query);
    }

    @Override
    public String toString() {
        return "DeferredVertexQuery{" +
                "vertices=" + vertices +
                '}';
    }
}
