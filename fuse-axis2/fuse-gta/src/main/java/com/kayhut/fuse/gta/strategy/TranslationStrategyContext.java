package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.ontology.Ontology;

/**
 * Created by benishue on 12-Mar-17.
 */
public class TranslationStrategyContext {

    public TranslationStrategyContext(PlanOpBase planOpBase, Plan plan, Ontology ontology) {
        this.planOpBase = planOpBase;
        this.plan = plan;
        this.ontology = ontology;
    }

    public PlanOpBase getPlanOpBase() {
        return planOpBase;
    }

    public Plan getPlan() {
        return plan;
    }

    public Ontology getOntology() {
        return ontology;
    }

    //region Fields
    private PlanOpBase planOpBase;
    private Plan plan;
    private Ontology ontology;
    //endregion
}
