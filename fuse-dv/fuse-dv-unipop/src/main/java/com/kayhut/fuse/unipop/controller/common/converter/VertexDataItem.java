package com.kayhut.fuse.unipop.controller.common.converter;

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
