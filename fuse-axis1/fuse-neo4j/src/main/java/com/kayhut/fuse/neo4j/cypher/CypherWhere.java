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
        sb.append(conditions.get(0).toString());
        for(int i=1; i<conditions.size(); i++) {
            sb.append(String.format(" %s %s",conditions.get(i).type,conditions.get(i).toString()));
        }
        return sb.toString();
    }

    public CypherWhere copy() {
        CypherWhere newWhere = new CypherWhere();
        for (CypherCondition c :
                conditions) {
            newWhere.conditions.add(c.copy());
        }
        return newWhere;
    }
}
