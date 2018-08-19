package com.kayhut.fuse.model.asgQuery;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;

/**
 * Created by benishue on 09-May-17.
 */
public class AsgStrategyContext {

    //region Ctrs
    public AsgStrategyContext(Ontology.Accessor ont) {
        this.ont = ont;
    }

    public AsgStrategyContext(Ontology.Accessor ont,Query query) {
        this.ont = ont;
        this.query = query;
    }
    //endregion

    //region Getters & Setters
    public Ontology.Accessor getOntologyAccessor() {
        return ont;
    }

    public Query getQuery() {
        return query;
    }
    //endregion

    //region Fields
    private Ontology.Accessor ont;
    private Query query;
    //endregion
}
