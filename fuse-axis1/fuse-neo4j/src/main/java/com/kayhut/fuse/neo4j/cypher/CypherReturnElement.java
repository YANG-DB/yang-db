package com.kayhut.fuse.neo4j.cypher;

/**
 * Created by Elad on 19/03/2017.
 */
public class CypherReturnElement {

    private String tag;
    private String alias;
    private String function;

    private CypherReturnElement() {

    }

    public static CypherReturnElement cypherReturnElement() {
        return new CypherReturnElement();
    }

    public CypherReturnElement withTag(String t) {
        tag = t;
        return this;
    }

    public CypherReturnElement withAlias(String aliasName) {
        alias = aliasName;
        return this;
    }

    public CypherReturnElement withFunction(String funcName) {
        function = funcName;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public String getAlias() {
        return alias;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(function != null) {
            sb.append(function + "(");
        }
        sb.append(tag);
        if(function != null) {
            sb.append(")");
        }
        if(alias != null) {
            sb.append(" AS " + alias);
        }
        return sb.toString();
    }

    public CypherReturnElement copy() {
        return cypherReturnElement().withTag(tag).withAlias(alias).withFunction(function);
    }

}
