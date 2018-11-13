package org.unipop.process.predicate;

/*-
 * #%L
 * ReceivesPredicatesHolder.java - unipop-core - kayhut - 2,016
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

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.query.predicates.PredicatesHolder;

public interface ReceivesPredicatesHolder<S, E> extends Step<S, E> {
    void addPredicate(PredicatesHolder predicatesHolder);
    PredicatesHolder getPredicates();
    void setLimit(int limit);
}
