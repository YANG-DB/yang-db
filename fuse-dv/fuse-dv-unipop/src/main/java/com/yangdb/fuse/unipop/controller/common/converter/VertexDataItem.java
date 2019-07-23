package com.yangdb.fuse.unipop.controller.common.converter;

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
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

/**
 * Created by roman.margolis on 14/03/2018.
 */
public class VertexDataItem implements DataItem {
    //region Constructors
    public VertexDataItem(Vertex vertex) {
        this.vertex = vertex;
    }
    //endregion

    //region DataItem Implementation
    @Override
    public Object id() {
        return vertex.id();
    }

    @Override
    public Map<String, Object> properties() {
        return Stream.ofAll(() -> this.vertex.properties())
                .toJavaMap(property -> new Tuple2<>(property.key(), property.value()));
    }
    //endregion

    //region Fields
    private Vertex vertex;
    //endregion
}
