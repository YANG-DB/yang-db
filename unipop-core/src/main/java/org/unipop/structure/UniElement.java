package org.unipop.structure;

/*-
 * #%L
 * UniElement.java - unipop-core - kayhut - 2,016
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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyProperty;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyVertexProperty;
import org.unipop.query.mutation.PropertyQuery;
import org.unipop.query.mutation.RemoveQuery;

import java.util.*;

public abstract class UniElement implements Element {
    protected String id;
    protected String label;
    protected UniGraph graph;

    public UniElement(Map<String, Object> properties, UniGraph graph) {
        this.graph = graph;

        this.id = (String)properties.remove(T.id.getAccessor());
        this.label = (String)properties.remove(T.label.getAccessor());
        if (this.label == null) {
            this.label = getDefaultLabel();
        }
    }

    protected abstract Map<String, Property> getPropertiesMap();

    protected abstract String getDefaultLabel();

    protected Property addPropertyLocal(String key, Object value) {
        try {
            ElementHelper.validateProperty(key, value);
            Property property = createProperty(key, value);
            getPropertiesMap().put(key, property);
            return property;
        } catch (IllegalArgumentException e) {
            //todo in case some mandatory fields do not exist allow empty property place holder
            final EmptyUniVertexProperty emptyVertexProperty = new EmptyUniVertexProperty() {
                @Override
                public String key() {
                    return key;
                }

                @Override
                public Object value() {
                    return value;
                }
            };
            getPropertiesMap().put(key, emptyVertexProperty);
            return emptyVertexProperty;
        }
    }

    @Override
    public Object id() {
        return this.id;
    }

    @Override
    public String label() {
        return this.label;
    }

    @Override
    public Graph graph() {
        return this.graph;
    }

    @Override
    public Set<String> keys() {
        return this.getPropertiesMap().keySet();
    }

    @Override
    public <V> Property<V> property(final String key) {
        return this.getPropertiesMap().containsKey(key) ? this.getPropertiesMap().get(key) : Property.<V>empty();
    }

    @Override
    public int hashCode() {
        return ElementHelper.hashCode(this);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    protected Iterator propertyIterator(String[] propertyKeys) {
        Map<String, Property> properties = this.getPropertiesMap();

        if (propertyKeys.length > 0)
            return properties.entrySet().stream().filter(entry -> ElementHelper.keyExists(entry.getKey(), propertyKeys)).map(x -> x.getValue()).iterator();

        return properties.values().iterator();
    }

    public void removeProperty(Property property) {
        getPropertiesMap().remove(property.key());
        PropertyQuery<UniElement> propertyQuery = new PropertyQuery<>(this, property, PropertyQuery.Action.Remove, null);
        this.graph.getControllerManager().getControllers(PropertyQuery.PropertyController.class).forEach(controller ->
                controller.property(propertyQuery));
    }

    protected abstract Property createProperty(String key, Object value);

    @Override
    public void remove() {
        RemoveQuery<UniElement> removeQuery = new RemoveQuery<>(Arrays.asList(this), null);
        this.graph.getControllerManager().getControllers(RemoveQuery.RemoveController.class).forEach(controller ->
                controller.remove(removeQuery));
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public UniGraph getGraph() {
        return graph;
    }

    public static <E extends Element> Map<String, Object> fullProperties(E element) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(T.id.getAccessor(), element.id());
        properties.put(T.label.getAccessor(), element.label());
        element.properties().forEachRemaining(property -> properties.put(property.key(), property.value()));

    return properties;
}

    @Override
    public String toString() {
        return "UniElement{" +
                "properties=" + getPropertiesMap() +
                ", id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", graph=" + graph +
                '}';
    }
}
