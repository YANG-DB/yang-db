package com.kayhut.fuse.unipop.schemaProviders;

import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Set;

public class EdgeSchemaSupplierContextUniImpl extends EdgeSchemaSupplierContextImpl {

    public EdgeSchemaSupplierContextUniImpl(VertexControllerContext context) {
        super(context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet(),
                toDirection(context.getDirection()),
                Stream.ofAll(context.getBulkVertices()).get(0).label(),
                context.getSchemaProvider()
                );
    }


    private static Rel.Direction toDirection(Direction direction){
        Rel.Direction res = Rel.Direction.RL;
        switch (direction){
            case OUT:
                res = Rel.Direction.R;
                break;
            case IN:
                res = Rel.Direction.L;
                break;

        }
        return res;
    }
}
