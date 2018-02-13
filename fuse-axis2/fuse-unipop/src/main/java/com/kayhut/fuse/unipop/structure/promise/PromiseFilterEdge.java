package com.kayhut.fuse.unipop.structure.promise;

import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

/**
 * Created by Roman on 5/9/2017.
 */
public class PromiseFilterEdge extends UniEdge {

    //region Constructor
    public PromiseFilterEdge(Vertex v,  UniGraph graph) {
        super(new MapBuilder<String, Object>()
                        .put(T.id.getAccessor(), GlobalConstants.Labels.PROMISE_FILTER + v.id()).get(),
                v,
                v,
                v,
                graph);
    }
    //endregion

    //region Override Methods
    @Override
    protected String getDefaultLabel() {
        return GlobalConstants.Labels.PROMISE_FILTER;
    }

    @Override
    public String toString() {
        return String.format(PRINT_FORMAT, outVertex.id(), id, inVertex.id());
    }
    //endregion

    //region Static
    private static String PRINT_FORMAT = "%s --(%s)--> %s";
    //endregion
}
