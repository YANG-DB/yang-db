package com.kayhut.fuse.unipop.descriptor;

import com.kayhut.fuse.dispatcher.descriptors.Descriptor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;

/**
 * Created by moti on 6/21/2017.
 */
public class GraphTraversalDescriptor implements Descriptor<DefaultGraphTraversal> {
    @Override
    public String describe(DefaultGraphTraversal query) {
        StringBuilder sb = new StringBuilder();
        for(Object step : query.asAdmin().getSteps()) {
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
