package com.kayhut.fuse.unipop.process.traversal.dsl.graph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 1/29/2018.
 */
public class __ {
    protected __() {
    }

    public static <A> GraphTraversal<A, A> start() {
        return new FuseGraphTraversal<>();
    }
}
