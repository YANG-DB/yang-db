package org.unipop.query.aggregation;

/*-
 * #%L
 * ReduceEdgeQuery.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.unipop.query.StepDescriptor;
import org.unipop.query.predicates.PredicatesHolder;

public class ReduceEdgeQuery extends ReduceQuery {
    public ReduceEdgeQuery(PredicatesHolder predicates, StepDescriptor stepDescriptor, PredicatesHolder vertexPredicates, Direction direction) {
        super(predicates, stepDescriptor);
        this.vertexPredicates = vertexPredicates;
        this.direction = direction;
    }

    public PredicatesHolder getVertexPredicates() {
        return vertexPredicates;
    }

    public Direction getDirection() {
        return direction;
    }

    private PredicatesHolder vertexPredicates;
    private Direction direction;
}
