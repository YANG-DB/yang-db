package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.ontology.Ontology;

/**
 * Created by benishue on 12-Mar-17.
 */
public class TranslationStrategyContext {
    //region Constructors
    public TranslationStrategyContext(Plan plan, Ontology ontology) {
        this.plan = plan;
        this.ontology = ontology;
    }
    //endregion

    //region Properties
    public Plan getPlan() {
        return plan;
    }

    public Ontology getOntology() {
        return ontology;
    }
    //endregion

    //region Fields
    private Plan plan;
    private Ontology ontology;
    //endregion
}
