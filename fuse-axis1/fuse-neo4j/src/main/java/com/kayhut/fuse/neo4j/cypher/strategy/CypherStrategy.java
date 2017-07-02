package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;

import java.util.Map;

/**
 * Created by Elad on 26/03/2017.
 */
public abstract class CypherStrategy {

    protected Map<AsgEBase, CypherCompilationState> state;
    protected Ontology ontology;

    public CypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
       state = compilationState;
       ontology = ont;
    }

    protected CypherCompilationState getRelevantState(AsgEBase element) {
        if(state.containsKey(element))
            return state.get(element);
        //TODO: manage multi parents in map
        return state.get(element.getParents().get(0));
    }

    protected CypherCompilationState context(AsgEBase element, CypherCompilationState compilationState) {
        state.put(element, compilationState);
        return compilationState;
    }

    public abstract CypherCompilationState apply(AsgEBase element);
}