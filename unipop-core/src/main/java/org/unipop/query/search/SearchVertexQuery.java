package org.unipop.query.search;

/*-
 *
 * SearchVertexQuery.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.javatuples.Pair;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.StepDescriptor;
import org.unipop.query.controller.UniQueryController;
import org.unipop.query.VertexQuery;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SearchVertexQuery extends SearchQuery<Edge> implements VertexQuery {

    private final List<Vertex> vertices;
    private final Direction direction;

    public SearchVertexQuery(Class<Edge> returnType, List<Vertex> vertices, Direction direction, PredicatesHolder predicates, int limit, Set<String> propertyKeys, List<Pair<String, Order>> orders, StepDescriptor stepDescriptor) {
        super(returnType, predicates, limit, propertyKeys, orders, stepDescriptor);
        this.vertices = vertices;
        this.direction = direction;
    }

    @Override
    public List<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    public interface SearchVertexController extends UniQueryController {
        Iterator<Edge> search(SearchVertexQuery uniQuery);
    }

    @Override
    public boolean test(Edge element, PredicatesHolder predicates) {
        boolean edgePredicates = super.test(element, predicates);
        if (!edgePredicates) return false;
        if (direction.equals(Direction.OUT) || direction.equals(Direction.BOTH)) {
            if (vertices.contains(element.outVertex())) return true;
        }
        if (direction.equals(Direction.IN) || direction.equals(Direction.BOTH)) {
            if (vertices.contains(element.inVertex())) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "SearchVertexQuery{" +
                "vertices=" + vertices +
                ", direction=" + direction +
                '}';
    }
}
