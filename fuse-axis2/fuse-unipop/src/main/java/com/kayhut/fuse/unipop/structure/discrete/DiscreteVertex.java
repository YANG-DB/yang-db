package com.kayhut.fuse.unipop.structure.discrete;

import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.structure.UniGraph;
import org.unipop.structure.UniVertex;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteVertex extends UniVertex {
    //region Constructor
    public DiscreteVertex(Object id, String label, UniGraph graph, Map<String, Object> properties) {
        super(new MapBuilder<>(properties)
                .put(T.id.getAccessor(), id)
                .put(T.label.getAccessor(), label)
                .get(), graph);
    }
    //endregion
}
