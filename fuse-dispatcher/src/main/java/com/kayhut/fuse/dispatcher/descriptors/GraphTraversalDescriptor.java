package com.kayhut.fuse.dispatcher.descriptors;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class GraphTraversalDescriptor implements Descriptor<GraphTraversal<?, ?>> {
    @Override
    public String describe(GraphTraversal<?, ?> item) {
        StringBuilder sb = new StringBuilder();
        for(Object step : item.asAdmin().getSteps()) {
            /*if (TraversalParent.class.isAssignableFrom(step.getClass())) {
                TraversalParent traversalParent = (TraversalParent)step;
                traversalParent.getGlobalChildren()
            } else {*/
            sb.append(step.toString());
            //}
            sb.append("\n");
        }
        return sb.toString();
    }
}
