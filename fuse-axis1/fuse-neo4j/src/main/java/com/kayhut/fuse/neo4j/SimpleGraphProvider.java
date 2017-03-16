package com.kayhut.fuse.neo4j;

import javaslang.Tuple2;
import org.neo4j.driver.v1.*;

/**
 * Created by liorp on 3/16/2017.
 */
public class SimpleGraphProvider implements GraphProvider {
    static String NEO4J_BOLT_URL = "bolt://localhost:7687";
    static String NEO4J_USER = "neo4j";
    static String NEO4J_PWD = "1234";

    @Override
    public Tuple2<Transaction,StatementResult> run(String cypher) {
        Driver driver = GraphDatabase.driver(NEO4J_BOLT_URL, AuthTokens.basic(NEO4J_USER, NEO4J_PWD));
        Session session = driver.session();
        Transaction tx = session.beginTransaction();
        StatementResult result = tx.run(cypher);
        //return results
        return new Tuple2<>(tx, result);
    }
}
