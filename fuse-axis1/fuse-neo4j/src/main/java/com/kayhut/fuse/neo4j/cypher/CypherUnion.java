package com.kayhut.fuse.neo4j.cypher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by User on 21/03/2017.
 */
public class CypherUnion {

    List<CypherStatement> statements = new LinkedList<>();
    List<String> knownReturnTags = new ArrayList<>();

    private CypherUnion() {

    }

    public static CypherUnion union() {
        return new CypherUnion();
    }

    public CypherUnion add(CypherStatement statement) {
        //if the new statement has unknown return elements, they must be removed for a successful union!
        if(statements.size() == 0) {
            knownReturnTags.addAll(statement.getReturns().getElements().stream().map(cre -> cre.getTag()).collect(Collectors.toList()));
        } else {
            knownReturnTags.removeIf(tag -> statement.getReturns().getElements().stream().map(cre -> cre.getTag()).collect(Collectors.toList()).contains(tag) == false);
        }
        statements.add(statement);
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(CypherStatement st : statements) {
            st.getReturns().getElements().removeIf(e -> !knownReturnTags.contains(e.getTag()));
            sb.append(st.toString());
            sb.append("\nUNION\n");
        }
        return sb.toString().substring(0,sb.length() - "UNION\n".length());
    }
}
