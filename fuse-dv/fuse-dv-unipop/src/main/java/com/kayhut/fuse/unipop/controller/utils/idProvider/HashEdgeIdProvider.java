package com.kayhut.fuse.unipop.controller.utils.idProvider;

import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalHashProvider;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.promise.TraversalPromise;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Roman on 15/05/2017.
 */
public class HashEdgeIdProvider implements EdgeIdProvider<String> {
    //region Constructors
    public HashEdgeIdProvider(Optional<TraversalConstraint> constraint) throws Exception {
        this.traversalHashProvider = new TraversalHashProvider(Object::toString, "MD5", 8);
        this.constraintHash = this.traversalHashProvider
                .getValue(constraint.map(TraversalPromise::getTraversal)
                .orElseGet(() -> TraversalConstraint.EMPTY.getTraversal()));
    }
    //endregion

    //region EdgeIdProvider Implementation
    @Override
    public String get(String edgeLabel, Vertex outV, Vertex inV, Map<String, Object> properties) {
        return this.traversalHashProvider.getValue(
                 __.and(__.has(T.label, edgeLabel),
                        __.has("outV.id", outV.id()),
                        __.has("inV.id", inV.id()),
                        __.has("constraintHash", constraintHash)));
    }
    //endregion

    //region Fields
    private String constraintHash;
    private TraversalHashProvider traversalHashProvider;
    //endregion
}
