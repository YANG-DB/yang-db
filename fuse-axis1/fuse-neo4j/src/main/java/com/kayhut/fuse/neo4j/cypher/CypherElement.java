package com.kayhut.fuse.neo4j.cypher;

import java.util.Map;

/**
 * Created by User on 19/03/2017.
 */
public abstract class CypherElement {

    String tag;
    String label;
    Map<String, String> inlineProps;

    protected String buildPropsStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(" {");
        inlineProps.forEach((k,v)-> sb.append(k + ": " + v + ","));
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("}");
        return sb.toString();
    }

    public abstract CypherElement copy();
}
