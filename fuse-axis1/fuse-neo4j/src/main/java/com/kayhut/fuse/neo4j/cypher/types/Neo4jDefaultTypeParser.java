package com.kayhut.fuse.neo4j.cypher.types;

/**
 * Created by Elad on 6/25/2017.
 */
public class Neo4jDefaultTypeParser implements Neo4jTypeParser {
    @Override
    public String getCypherExpression(Object expression) {
        return String.valueOf(expression);
    }

    @Override
    public String getRealExpression(Object cypherExpression) {
        return String.valueOf(cypherExpression);
    }
}
