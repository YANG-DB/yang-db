package com.kayhut.fuse.unipop.structure.discrete;

import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.unipop.structure.UniGraph;
import org.unipop.structure.UniVertex;
import org.unipop.structure.UniVertexProperty;

import java.util.*;
import java.util.stream.Collectors;

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
}
