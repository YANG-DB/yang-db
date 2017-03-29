package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.neo4j.cypher.CypherElement;
import com.kayhut.fuse.neo4j.cypher.CypherNode;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;
import javaslang.Tuple2;

import java.util.Map;

import static com.kayhut.fuse.neo4j.cypher.CypherNode.EMPTY;

/**
 * Created by User on 26/03/2017.
 */
public class InitializeCypherStrategy extends CypherStrategy {

    public InitializeCypherStrategy(Ontology ontology, Map<AsgEBase, Tuple2<CypherStatement, String>> cypherStatementsMap) {
        super(ontology,cypherStatementsMap);
    }

    public CypherStatement apply(AsgEBase element) {
        //start node - initialize an empty cypher statement, with one path
        if (element.geteBase() instanceof Start) {
            return context(element,CypherStatement.cypherStatement());
        }
        return getWorkingStatement(element)._1();
    }
}

