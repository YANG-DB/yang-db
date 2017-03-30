package com.kayhut.fuse.neo4j.cypher;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.neo4j.cypher.strategy.CypherStrategiesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by EladW on 23/02/2017.
 */
public class CypherCompiler {

    static Logger logger = LoggerFactory.getLogger("com.kayhut.fuse.neo4j.cypher.CypherCompiler");

    //Traverses a tree of query elements, building the cypher statements along the way.
    //Each leaf node will contain a complete ready-to-run cypher statement, describing the single path from the root node to this leaf node.
    private static void cypherTreeTraverse(Queue<AsgEBase> nodesQueue,
                                           CypherStrategiesFactory strategiesFactory, Ontology ontology) {

        if(nodesQueue.isEmpty()) return;

        AsgEBase curNode = nodesQueue.poll();

        //apply the appropriate strategy for this type of node
        strategiesFactory.applyStrategy(curNode);

        //recursively apply to children
        curNode.getNext().forEach(child -> nodesQueue.add((AsgEBase) child));

        cypherTreeTraverse(nodesQueue, strategiesFactory, ontology);

    }

    public static String compile(AsgQuery asgQuery, Ontology ontology) {

        AsgEBase<Start> start = asgQuery.getStart();

        Queue<AsgEBase> q = new LinkedList<>();

        q.add(start);

        Map<AsgEBase, CypherCompilationState> compilationState = new HashMap<>();

        //initialize cypher strategies
        CypherStrategiesFactory strategiesFactory = new CypherStrategiesFactory(compilationState, ontology);

        cypherTreeTraverse(q, strategiesFactory, ontology);

        CypherUnion union = CypherUnion.union();

        compilationState.values()
                .stream()
                .map(t -> t.getStatement())
                .distinct()
                .forEach(st -> union.add(st));

        String finalCypher = union.toString();

        logger.info(String.format("\n(%s) -[:Compiled]-> ( %s)", asgQuery.getName(), finalCypher));

        return finalCypher;

    }

}
