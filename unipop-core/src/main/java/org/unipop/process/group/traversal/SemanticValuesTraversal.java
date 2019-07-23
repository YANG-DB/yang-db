package org.unipop.process.group.traversal;

/*-
 * #%L
 * SemanticValuesTraversal.java - unipop-core - yangdb - 2,016
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

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

/**
 * Created by Gilad on 02/11/2015.
 */
public class SemanticValuesTraversal implements Traversal {
    public enum Type {
        property
    }

    //region Constructor
    public SemanticValuesTraversal(SemanticValuesTraversal.Type type, String key) {
        this.type = type;
        this.key = key;
    }
    //endregion

    //region Traversal Implementation
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        throw FastNoSuchElementException.instance();
    }
    //endregion

    //region Properties
    public String getKey() {
        return this.key;
    }

    public SemanticValuesTraversal.Type getType() {
        return this.type;
    }
    //endregion

    //region Fields
    private String key;
    private SemanticValuesTraversal.Type type;
    //endregion
}
