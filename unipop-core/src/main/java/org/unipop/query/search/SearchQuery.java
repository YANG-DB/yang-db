package org.unipop.query.search;

/*-
 * #%L
 * unipop-core
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

/*-
 *
 * SearchQuery.java - unipop-core - yangdb - 2,016
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
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.javatuples.Pair;
import org.unipop.query.predicates.PredicateQuery;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.StepDescriptor;
import org.unipop.query.controller.UniQueryController;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SearchQuery<E extends Element> extends PredicateQuery<E> {
    private final Class<E> returnType;
    private final int limit;
    private Set<String> propertyKeys;
    private List<Pair<String, Order>> orders;
    private String context;

    public SearchQuery(Class<E> returnType, PredicatesHolder predicates, int limit, Set<String> propertyKeys, List<Pair<String, Order>> orders, StepDescriptor stepDescriptor) {
        this(returnType,predicates,limit,propertyKeys,orders,"Generic",stepDescriptor);
    }

    public SearchQuery(Class<E> returnType, PredicatesHolder predicates, int limit, Set<String> propertyKeys, List<Pair<String, Order>> orders,String context, StepDescriptor stepDescriptor) {
        super(predicates, stepDescriptor);
        this.returnType = returnType;
        this.limit = limit;
        this.propertyKeys = propertyKeys;
        this.orders = orders;
        this.context = context;
    }

    public Class<E> getReturnType(){
        return returnType;
    }

    public Set<String> getPropertyKeys() {
        return this.propertyKeys;
    }

    public int getLimit(){
        return limit;
    }

    public String getContext() {
        return context;
    }

    public List<Pair<String, Order>> getOrders() {
        return orders;
    }

    public interface SearchController extends UniQueryController {
        <E extends Element> Iterator<E> search(SearchQuery<E> uniQuery);
    }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "returnType=" + returnType +
                ", context=" + context +
                ", limit=" + limit +
                '}';
    }
}
