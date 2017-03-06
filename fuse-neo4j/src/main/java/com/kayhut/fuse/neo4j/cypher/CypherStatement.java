package com.kayhut.fuse.neo4j.cypher;

import java.util.ArrayList;

/**
 * Created by elad on 06/03/2017.
 */
public class CypherStatement {

    private ArrayList<CypherMatch> matches;
    private CypherReturn returns;
    private CypherWhere where;

    public static CypherStatement build() {
        return new CypherStatement();
    }

    public CypherStatement with(ArrayList<CypherMatch> match) {
        matches = match;
        return this;
    }

    public CypherStatement with(CypherMatch match) {
        if(matches == null) {
            matches = new ArrayList<>();
        }
        matches.add(match);
        return this;
    }

    public CypherStatement with(CypherReturn ret) {
        returns = ret;
        return this;
    }

    public CypherStatement with(CypherWhere wheres) {
        where = wheres;
        return this;
    }

    public String compose() {
        StringBuilder builder = new StringBuilder();
        int nmatches = matches.size();
        for (int i = 0; i < nmatches; i++) {
            CypherMatch match = matches.get(i);
            if(i < nmatches - 1) {
                builder.append(match + ",");
            }
            else {
                builder.append(match);
            }
        }
        if(where != null) {
            builder.append(where);
        }
        builder.append(returns);
        return builder.toString();
    }

}
