package com.kayhut.fuse.neo4j.cypher;


import java.util.LinkedList;
import java.util.List;

/**
 * Created by elad on 06/03/2017.
 */
public class CypherWhere {

    List<CypherCondition> conditions = new LinkedList<>();

    private CypherWhere() {

    }

    public static CypherWhere cypherWhere() { return new CypherWhere();}

    public CypherWhere withCondition(CypherCondition cond) {
        conditions.add(cond);
        return this;
    }

    @Override
    public String toString() {
        if(conditions.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("WHERE ");
        conditions.forEach(c -> sb.append(c.toString() + ","));
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    public CypherWhere copy() {
        CypherWhere newWhere = new CypherWhere();
        newWhere.conditions = conditions;
        return newWhere;
    }
}
