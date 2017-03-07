package com.kayhut.fuse.neo4j.cypher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elad on 06/03/2017.
 */
public class CypherStatement {

    private CypherMatch match;
    private CypherReturn returns;
    private CypherWhere where;
    private List<CypherStatement> union;

    public CypherStatement() {
        match = new CypherMatch();
        returns = new CypherReturn();
        where = new CypherWhere();
    }

    public CypherStatement copy() {
        CypherStatement newSt = new CypherStatement();
        newSt.match = new CypherMatch(match.toString());
        newSt.where = new CypherWhere(where.toString());
        newSt.returns = new CypherReturn(returns.toString());
        return newSt;
    }

    public CypherMatch getMatch() {
        return match;
    }

    public CypherReturn getReturn() {
        return returns;
    }

    public CypherWhere getWhere() {
        return where;
    }

    public void and(CypherStatement other) {
        match = match.and(other.match);
    }

    public void or(CypherStatement other) {
        if (union == null) {
            union = new ArrayList<>();
        }
        union.add(other);
    }

    public static CypherStatement and(List<CypherStatement> statements) {
        CypherStatement st = statements.get(0);
        for(int i=1; i<statements.size(); i++) {
            st.and(statements.get(i));
        }
        return st;
    }

    public static CypherStatement or(List<CypherStatement> statements) {
        CypherStatement st = statements.get(0);
        for(int i=1; i<statements.size(); i++) {
            st.or(statements.get(i));
        }
        return st;
    }

    public String compose() {
        StringBuilder builder = new StringBuilder();
        builder.append(match);
        if(where != null) {
            builder.append(where);
        }
        builder.append(returns);
        if(union != null) {
            for (CypherStatement u :
                    union) {
                builder.append("\nUNION\n" + u.compose());
            }
        }
        return builder.toString();
    }

}
