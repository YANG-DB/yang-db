package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.aggregation.AggL1;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;
import com.kayhut.fuse.neo4j.cypher.CypherCompilationState;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Elad on 3/30/2017.
 */
public class CypherStrategiesFactory {

    private Map<Class, CypherStrategy> cypherStrategyMap;
    private CypherStrategy defaultStrategy;

    public CypherStrategiesFactory(Map<AsgEBase, CypherCompilationState> compilationState, Ontology ont) {
        cypherStrategyMap = new HashMap<>();
        cypherStrategyMap.put(Start.class, new InitializeCypherStrategy(compilationState, ont));
        cypherStrategyMap.put(ETyped.class, new TypedNodeCypherStrategy(compilationState, ont));
        cypherStrategyMap.put(Rel.class, new TypedRelCypherStrategy(compilationState, ont));
        cypherStrategyMap.put(EProp.class, new ConditionCypherStrategy(compilationState, ont));
        cypherStrategyMap.put(RelProp.class, new ConditionCypherStrategy(compilationState, ont));
        cypherStrategyMap.put(Quant1.class, new Quant1CypherStrategy(compilationState, ont));
        cypherStrategyMap.put(Quant2.class, new Quant2CypherStrategy(compilationState, ont));
        cypherStrategyMap.put(EConcrete.class, new ConcreteNodeCypherStrategy(compilationState, ont));
        cypherStrategyMap.put(AggL1.class, new AggL1CypherStrategy(compilationState, ont));
        defaultStrategy = new DefaultCypherStrategy(compilationState, ont);
    }

    public CypherStrategy supplyStrategy(AsgEBase asgNode) {
        if(cypherStrategyMap.containsKey(asgNode.geteBase().getClass())) {
            return cypherStrategyMap.get(asgNode.geteBase().getClass());
        } else {
            return defaultStrategy;
        }
    }

    public CypherCompilationState applyStrategy(AsgEBase asgNode) {
       return supplyStrategy(asgNode).apply(asgNode);
    }

}
