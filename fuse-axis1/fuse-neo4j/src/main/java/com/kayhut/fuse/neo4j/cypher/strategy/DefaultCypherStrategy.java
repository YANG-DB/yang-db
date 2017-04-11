package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;

import java.util.Map;

/**
 * Created by Elad on 4/2/2017.
 */
public class DefaultCypherStrategy extends CypherStrategy  {

    public DefaultCypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        super(compilationState, ont);
    }

    @Override
    public CypherCompilationState apply(AsgEBase element) {
        return context(element, getRelevantState(element));
    }
}
