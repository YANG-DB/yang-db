package com.kayhut.fuse.unipop.structure.discrete;

import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

import java.util.Map;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class DiscreteEdge extends UniEdge {
    //region Constructors
    public DiscreteEdge(Object id, String label, Vertex outV, Vertex inV, UniGraph graph, Map<String, Object> properties) {
        super(new MapBuilder<>(properties).put(T.id.getAccessor(), id).put(T.label.getAccessor(), label)
                .get(), outV, inV, graph);
    }
    //endregion
}
