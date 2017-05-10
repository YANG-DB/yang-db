package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.ontology.Ontology;

/**
 * Created by benishue on 09-May-17.
 */
public class AsgStrategyContext {

    //region Ctrs
    public AsgStrategyContext() {
    }

    public AsgStrategyContext(Ontology ontology) {
        this.ontology = ontology;
    }
    //endregion

    //region Getters & Setters
    public Ontology getOntology() {
        return ontology;
    }
    //endregion

    //region Fields
    private Ontology ontology;
    //endregion
}
