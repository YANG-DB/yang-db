package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Elad on 3/30/2017.
 */
public class CypherStrategiesFactory {

    private Map<Class, CypherStrategy> cypherStrategyMap;

    public CypherStrategiesFactory(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        cypherStrategyMap = new HashMap<>();
        cypherStrategyMap.put(Start.class, new InitializeCypherStrategy(compilationState, ont));
        cypherStrategyMap.put(ETyped.class, new TypedNodeCypherStrategy(compilationState, ont));
        cypherStrategyMap.put(Rel.class, new TypedRelCypherStrategy(compilationState, ont));
        cypherStrategyMap.put(EProp.class, new ConditionCypherStrategy(compilationState, ont));
    }

    public CypherStrategy supplyStrategy(AsgEBase asgNode) {
        return cypherStrategyMap.get(asgNode.geteBase().getClass());
    }

    public CypherCompilationState applyStrategy(AsgEBase asgNode) {
        return cypherStrategyMap.get(asgNode.geteBase().getClass()).apply(asgNode);
    }

}
