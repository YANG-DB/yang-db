package com.kayhut.fuse.neo4j.cypher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 21/03/2017.
 */
public class CypherUnion {

    List<CypherStatement> statements = new ArrayList<>();

    private CypherUnion() {

    }

    public static CypherUnion union() {
        return new CypherUnion();
    }

    public CypherUnion add(CypherStatement statement) {
        statements.add(statement);
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(CypherStatement st : statements) {
            sb.append(st.toString());
            sb.append("\nUNION\n");
        }
        return sb.toString().substring(0,sb.length() - "UNION\n".length());
    }
}
