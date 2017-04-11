package com.kayhut.fuse.neo4j.cypher;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;

/**
 * Created by Elad on 3/30/2017.
 */
public class CypherCompilationState {

    private AsgQuery asgQuery;
    private Ontology ontology;
    private CypherStatement statement;
    private String pathTag;

    public CypherCompilationState(CypherStatement statement, String pathTag) {
        this.pathTag = pathTag;
        this.statement = statement;
    }

    public AsgQuery getAsgQuery() {
        return asgQuery;
    }

    public void setAsgQuery(AsgQuery asgQuery) {
        this.asgQuery = asgQuery;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public CypherStatement getStatement() {
        return statement;
    }

    public void setStatement(CypherStatement statement) {
        this.statement = statement;
    }

    public String getPathTag() {
        return pathTag;
    }

    public void setPathTag(String pathTag) {
        this.pathTag = pathTag;
    }
}
