package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;

import java.util.Map;

/**
 * Created by Elad on 26/03/2017.
 */
public class InitializeCypherStrategy extends CypherStrategy {

    public InitializeCypherStrategy(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        super(compilationState, ont);
    }

    public CypherCompilationState apply(AsgEBase element) {
        //start node - initialize an empty cypher statement, with one path.
        if (element.geteBase() instanceof Start) {
            CypherStatement statement = CypherStatement.cypherStatement();
            String path = statement.getNextPathTag();
            return context(element, new CypherCompilationState(statement, path));
        }
        return getRelevantState(element);
    }
}

