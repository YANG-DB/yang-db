package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherElement;
import com.kayhut.fuse.neo4j.cypher.CypherNode;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;
import javaslang.Tuple2;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 26/03/2017.
 */
public abstract class CypherStrategy {
    private Map<AsgEBase, Tuple2<CypherStatement, String>> cypherStatementsMap;
    protected Ontology ontology;

    public CypherStrategy(Ontology ontology, Map<AsgEBase, Tuple2<CypherStatement, String>> cypherStatementsMap) {
        this.ontology = ontology;
        this.cypherStatementsMap = cypherStatementsMap;
    }

    protected Tuple2<CypherStatement, String> getWorkingStatement(AsgEBase element) {
        if(cypherStatementsMap.containsKey(element))
            return cypherStatementsMap.get(element);
        //todo manage multi parents in map
        return cypherStatementsMap.get(element.getParents().get(0));
    }

    protected CypherStatement context(AsgEBase element, CypherStatement newStatement) {
        cypherStatementsMap.put(element, new Tuple2(newStatement, newStatement.getNextPathTag()));
        return newStatement;
    }

    public abstract CypherStatement apply(AsgEBase element);
}