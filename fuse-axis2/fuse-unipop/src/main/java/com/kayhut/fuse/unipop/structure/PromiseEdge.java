package com.kayhut.fuse.unipop.structure;

import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by r on 11/16/2015.
 */
public class PromiseEdge extends UniEdge{

    //region Constructor
    public PromiseEdge(Object id, Vertex outV, Vertex inV, Map<String, Object> properties, UniGraph graph) {
        super(new MapBuilder<>(properties).put(T.id.getAccessor(), id).get(), outV, inV, graph);
    }
    //endregion

    //region Override Methods
    @Override
    protected String getDefaultLabel() {
        return GlobalConstants.Labels.PROMISE;
    }

    @Override
    public String toString() {
        return String.format(PRINT_FORMAT, outVertex.id(), id, property(GlobalConstants.HasKeys.COUNT), inVertex.id());
    }
    //endregion

    //region Static
    private static String PRINT_FORMAT = "%s --(%s: %s)--> %s";
    //endregion
}
