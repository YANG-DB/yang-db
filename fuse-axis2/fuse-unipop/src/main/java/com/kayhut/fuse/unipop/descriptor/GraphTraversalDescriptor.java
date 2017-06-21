package com.kayhut.fuse.unipop.descriptor;

import com.kayhut.fuse.model.descriptor.Descriptor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;

/**
 * Created by moti on 6/21/2017.
 */
public class GraphTraversalDescriptor implements Descriptor<DefaultGraphTraversal> {
    @Override
    public String name(DefaultGraphTraversal query) {
        return query.toString();
    }

    @Override
    public String describe(DefaultGraphTraversal query) {
        return query.toString();
    }
}
