package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.ontology.Ontology;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

/**
 * Created by benishue on 12-Mar-17.
 */
public class TranslationContext {
    //region Constructors
    public TranslationContext(Ontology ontology, GraphTraversalSource graphTraversalSource) {
        this.ontology = ontology;
        this.graphTraversalSource = graphTraversalSource;
    }
    //endregion

    //region Properties
    public Ontology getOntology() {
        return ontology;
    }

    public GraphTraversalSource getGraphTraversalSource() {
        return this.graphTraversalSource;
    }
    //endregion

    //region Fields
    private Ontology ontology;
    private GraphTraversalSource graphTraversalSource;
    //endregion
}
