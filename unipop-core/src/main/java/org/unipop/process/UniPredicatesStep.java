package org.unipop.process;

/*-
 * #%L
 * UniPredicatesStep.java - unipop-core - kayhut - 2,016
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

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.unipop.process.properties.PropertyFetcher;
import org.unipop.structure.UniGraph;

import java.util.HashSet;
import java.util.Set;

public abstract class UniPredicatesStep<S, E> extends UniBulkStep<S, E> implements PropertyFetcher {

    protected Set<String> propertyKeys;

    public UniPredicatesStep(Traversal.Admin traversal, UniGraph graph) {
        super(traversal, graph);
        this.propertyKeys = new HashSet<>();
    }

    @Override
    public void addPropertyKey(String key) {
        if (propertyKeys != null)
            propertyKeys.add(key);
    }

    @Override
    public void fetchAllKeys() {
        this.propertyKeys = null;
    }

    @Override
    public Set<String> getKeys() {
        return propertyKeys;
    }
}
