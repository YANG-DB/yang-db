package com.kayhut.fuse.neo4j.cypher.types;

/**
 * Created by Elad on 6/25/2017.
 */
public class Neo4jStringTypeParser implements Neo4jTypeParser {

    private final String CYPHER_STRING_FORMAT = "'%s'";

    @Override
    public String getCypherExpression(Object expression) {
        return String.format(CYPHER_STRING_FORMAT, expression);
    }

    @Override
    public String getRealExpression(Object cypherResult) {
        return String.valueOf(cypherResult);
    }
}
