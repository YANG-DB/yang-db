package org.unipop.schema.element;

/*-
 * #%L
 * VertexSchema.java - unipop-core - kayhut - 2,016
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

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;
import org.unipop.structure.UniVertex;

import java.util.*;
import java.util.stream.Collectors;

public interface VertexSchema extends ElementSchema<Vertex> {

    @Override
    default Collection<Vertex> fromFields(Map<String, Object> fields) {
        Vertex element = createElement(fields);
        if(element == null) return null;
        return Collections.singleton(element);
    }

    Vertex createElement(Map<String, Object> fields);

    default PredicatesHolder toPredicates(List<? extends Vertex> vertices) {
        if(vertices == null || vertices.size() == 0) return PredicatesHolderFactory.abort();
        HashSet<PredicatesHolder> predicates = new HashSet<>();
        vertices.stream().collect(Collectors.groupingBy(Vertex::label)).forEach((label, labelVertices) -> {
            HasContainer labelPredicate = new HasContainer(T.label.getAccessor(), P.eq(label));
            HasContainer ids = new HasContainer(T.id.getAccessor(),
                    P.within(labelVertices.stream().map(Vertex::id).collect(Collectors.toSet())));
            PredicatesHolder labelPredicates = PredicatesHolderFactory.and(ids, labelPredicate);
            PredicatesHolder toPredicates = toPredicates(labelPredicates);
            predicates.add(toPredicates);
        });
        return PredicatesHolderFactory.or(predicates);
    }
}
