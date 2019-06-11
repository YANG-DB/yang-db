package org.unipop.structure;

/*-
 * #%L
 * UniVertexProperty.java - unipop-core - kayhut - 2,016
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

import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.*;

import java.util.Iterator;

public class UniVertexProperty<V> implements VertexProperty<V> {

    private final UniVertex vertex;
    private final String key;
    private final V value;

    public UniVertexProperty(final UniVertex vertex, final String key, final V value) {
        this.vertex = vertex;
        this.key = key;
        this.value = value;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public V value() {
        return this.value;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public String toString() {
        return StringFactory.propertyString(this);
    }

    @Override
    public Object id() {
        return (long) (this.key.hashCode() + this.value.hashCode() + this.vertex.id().hashCode());
    }

    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    @Override
    public int hashCode() {
        return ElementHelper.hashCode((Element) this);
    }

    @Override
    public <U> Property<U> property(final String key, final U value) {
        throw VertexProperty.Exceptions.multiPropertiesNotSupported();
    }

    @Override
    public Vertex element() {
        return this.vertex;
    }

    @Override
    public <U> Iterator<Property<U>> properties(String... propertyKeys) {
        throw VertexProperty.Exceptions.multiPropertiesNotSupported();
    }

    @Override
    public void remove() {
        vertex.removeProperty(this);
    }
}
