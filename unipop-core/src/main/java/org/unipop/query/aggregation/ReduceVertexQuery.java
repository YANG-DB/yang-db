package org.unipop.query.aggregation;

/*-
 * #%L
 * ReduceVertexQuery.java - unipop-core - kayhut - 2,016
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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.StepDescriptor;
import org.unipop.query.controller.UniQueryController;
import org.unipop.query.VertexQuery;

import java.util.List;

public class ReduceVertexQuery extends ReduceQuery implements VertexQuery {

    private final List<Vertex> vertices;
    private final Direction direction;

    public ReduceVertexQuery(List<Vertex> vertices, Direction direction, PredicatesHolder predicates, StepDescriptor stepDescriptor) {
        super(predicates, stepDescriptor);
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

    public interface ReduceVertexController extends UniQueryController {
        void query(ReduceVertexQuery uniQuery);
    }

    @Override
    public String toString() {
        return "ReduceVertexQuery{" +
                "vertices=" + vertices +
                ", direction=" + direction +
                '}';
    }
}
