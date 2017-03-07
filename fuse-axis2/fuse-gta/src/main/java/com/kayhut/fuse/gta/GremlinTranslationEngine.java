package com.kayhut.fuse.gta;

import com.google.inject.Inject;
import com.kayhut.fuse.gta.translation.PlanOpTranslatorFactoryImpl;
import com.kayhut.fuse.model.execution.plan.Plan;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.util.EmptyTraversal;

/**
 * Created by moti on 3/7/2017.
 */
public class GremlinTranslationEngine {

    public GremlinTranslationEngine(PlanOpTranslatorFactoryImpl planOpTranslatorFactory) {
        this.planOpTranslatorFactory = planOpTranslatorFactory;
    }

    @Inject
    private PlanOpTranslatorFactoryImpl planOpTranslatorFactory;

    public Traversal CreateTraversal(Plan plan){
        // Create initial traversal

        // iterate ops
        // translate each op via factory
        
        return null;
    }
}
