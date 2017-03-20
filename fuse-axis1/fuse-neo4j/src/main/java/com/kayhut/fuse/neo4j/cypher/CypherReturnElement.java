package com.kayhut.fuse.neo4j.cypher;

/**
 * Created by User on 19/03/2017.
 */
public class CypherReturnElement {

    private CypherElement element;
    private String alias;
    private String function;

    private CypherReturnElement(CypherElement e) {
        element = e;
    }

    public static CypherReturnElement cypherReturnElement(CypherElement e) {
        return new CypherReturnElement(e);
    }

    public CypherReturnElement withAlias(String aliasName) {
        alias = aliasName;
        return this;
    }

    public CypherReturnElement withFunction(String funcName) {
        funcName = funcName;
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(function != null) {
            sb.append(function + "(");
        }
        sb.append(element.tag);
        if(function != null) {
            sb.append(")");
        }
        if(alias != null) {
            sb.append(" AS " + alias);
        }
        return sb.toString();
    }

}
