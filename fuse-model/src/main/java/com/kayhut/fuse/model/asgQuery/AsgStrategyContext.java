package com.kayhut.fuse.model.asgQuery;

import com.kayhut.fuse.model.ontology.Ontology;

/**
 * Created by benishue on 09-May-17.
 */
public class AsgStrategyContext {

    //region Ctrs
    public AsgStrategyContext(Ontology.Accessor ont) {
        this.ont = ont;
    }
    //endregion

    //region Getters & Setters
    public Ontology.Accessor getOntologyAccessor() {
        return ont;
    }
    //endregion

    //region Fields
    private Ontology.Accessor ont;
    //endregion
}
