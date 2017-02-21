package com.kayhut.fuse.neo4j.driver;

import org.neo4j.driver.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by EladW on 19/02/2017.
 */
public class Neo4jDriver {

    static String NEO4J_BOLT_URL = "bolt://localhost:7687";

    static String NEO4J_USER = "neo4j";

    static String NEO4J_PWD = "1234";

    Logger logger = LoggerFactory.getLogger("com.kayhut.fuse.neo4j.driver.Neo4jDriver");

    public String query(String cypherQuery) {

        StringBuilder result = new StringBuilder();

        Driver driver = GraphDatabase.driver(NEO4J_BOLT_URL, AuthTokens.basic( NEO4J_USER, NEO4J_PWD ) );

        Session session = driver.session();

        Transaction tx = session.beginTransaction();

        StatementResult res = tx.run(cypherQuery);

        while(res.hasNext()) {
            result.append(res.next());
        }

        return result.toString();
    }
}
