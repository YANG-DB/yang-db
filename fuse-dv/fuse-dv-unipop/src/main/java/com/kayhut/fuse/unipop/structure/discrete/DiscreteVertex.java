package com.kayhut.fuse.unipop.structure.discrete;

/*-
 * #%L
 * fuse-dv-unipop
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

import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.unipop.query.StepDescriptor;
import org.unipop.query.mutation.PropertyQuery;
import org.unipop.structure.UniGraph;
import org.unipop.structure.UniVertex;
import org.unipop.structure.UniVertexProperty;

import java.util.*;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteVertex extends UniVertex {
    //region Constructor
    public DiscreteVertex(Object id, String label, UniGraph graph, Map<String, Object> properties) {
        super(Collections.emptyMap(), graph);
        this.graph = graph;
        this.id = id.toString();
        this.label = label;

        this.properties =
                Stream.ofAll(properties == null ? Collections.emptyList() : properties.entrySet())
                .map(entry -> new UniVertexProperty<>(this, entry.getKey(), entry.getValue()))
                .toJavaMap(vertexProperty -> new Tuple2<>(vertexProperty.key(), Collections.singletonList(vertexProperty)));
    }
    //endregion

    //region Override Methods
    public <V> VertexProperty<V> property(String key, V value) {
        UniVertexProperty vertexProperty = (UniVertexProperty)this.addPropertyLocal(key, value);
        return vertexProperty;
    }

    @Override
    public <V> Iterator<VertexProperty<V>> properties(final String... propertyKeys) {
        List<VertexProperty<V>> propertyList = new ArrayList<>(this.properties.size());

        if (propertyKeys.length == 0) {
            for(List<VertexProperty> vertexPropertyList : this.properties.values()) {
                propertyList.add(vertexPropertyList.get(0));
            }
        } else {
            Set<String> propertyKeysSet = Stream.of(propertyKeys).toJavaSet();
            for(List<VertexProperty> vertexPropertyList : this.properties.values()) {
                VertexProperty<V> vertexProperty = vertexPropertyList.get(0);
                if (propertyKeysSet.contains(vertexProperty.key())) {
                    propertyList.add(vertexProperty);
                }
            }
        }

        return propertyList.iterator();
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object) &&
                this.properties.keySet().equals(((DiscreteVertex)object).properties.keySet());
    }
    //endregion
}
