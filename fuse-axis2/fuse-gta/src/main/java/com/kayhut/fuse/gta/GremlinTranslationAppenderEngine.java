package com.kayhut.fuse.gta;

import com.google.inject.Inject;
import com.kayhut.fuse.gta.translation.SimplePlanOpTranslator;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.PromiseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.Collection;

/**
 * Created by moti on 3/7/2017.
 */
public class GremlinTranslationAppenderEngine {

    @Inject
    public GremlinTranslationAppenderEngine(Ontology ontology) {
        this.simplePlanOpTranslator = new SimplePlanOpTranslator(new PromiseGraph());
        this.ontology = ontology;
    }

    private SimplePlanOpTranslator simplePlanOpTranslator;

    public Traversal CreateTraversal(Plan plan){
        // Create initial traversal
        GraphTraversal graphTraversal = __.start();
        return simplePlanOpTranslator.translate(plan,graphTraversal, this.ontology);
    }

    private Ontology ontology;
}
