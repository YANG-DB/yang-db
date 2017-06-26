package com.kayhut.fuse.neo4j.cypher.types;

/**
 * Created by Elad on 6/25/2017.
 */
public interface Neo4jTypeParser {
    String getCypherExpression(Object expression);
    String getRealExpression(Object cypherResult);
}
