package com.kayhut.fuse.gta;

import com.google.inject.Inject;
import com.kayhut.fuse.gta.translation.SimplePlanOpTranslator;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.controller.UniGraphProvider;
import com.kayhut.fuse.unipop.promise.PromiseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.unipop.structure.UniGraph;

/**
 * Created by moti on 3/7/2017.
 */
public class GremlinTranslationAppenderEngine {

    @Inject
    public GremlinTranslationAppenderEngine(UniGraphProvider graphProvider) {
        this.simplePlanOpTranslator = new SimplePlanOpTranslator(graphProvider.getGraph());
    }


    public Traversal createTraversal(Ontology ontology, Plan plan){
        // Create initial traversal
        GraphTraversal graphTraversal = __.start();
        return simplePlanOpTranslator.translate(plan,graphTraversal, ontology);
    }

    private SimplePlanOpTranslator simplePlanOpTranslator;
}
