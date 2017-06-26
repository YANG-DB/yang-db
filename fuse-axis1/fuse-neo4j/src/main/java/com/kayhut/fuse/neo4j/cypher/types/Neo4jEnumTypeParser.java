package com.kayhut.fuse.neo4j.cypher.types;

/**
 * Created by Elad on 6/25/2017.
 */
public class Neo4jEnumTypeParser implements Neo4jTypeParser {
    @Override
    public String getCypherExpression(Object expression) {
        //Currently, enums are saved as strings
        return String.valueOf(expression);
    }

    @Override
    public String getRealExpression(Object cypherResult) {
        return String.valueOf(cypherResult);
    }
}
