package com.kayhut.fuse.unipop.descriptor;

import com.kayhut.fuse.model.descriptor.Descriptor;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;

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
