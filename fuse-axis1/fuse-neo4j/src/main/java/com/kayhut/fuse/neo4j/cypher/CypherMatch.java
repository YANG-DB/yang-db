package com.kayhut.fuse.neo4j.cypher;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by elad on 06/03/2017.
 */
public class CypherMatch {

    private Map<String, CypherPath> paths = new HashMap<>();

    private CypherMatch() {

    }

    public static CypherMatch cypherMatch() {return new CypherMatch();}

    public CypherMatch addPath(CypherPath path){
        paths.put(path.tag, path);
        return this;
    }

    @Override
    public String toString() {
        if(paths.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("MATCH\n");
        paths.values().forEach(p -> sb.append(String.format("%s\n,",p)));
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.deleteCharAt(sb.lastIndexOf("\n"));
        return sb.toString();
    }

    public CypherMatch copy() {
        CypherMatch newMatch = new CypherMatch();
        for (CypherPath cp :
                paths.values()) {
            newMatch.paths.put(cp.tag, cp.copy());
        }
        return newMatch;
    }

    public Map<String, CypherPath> getPaths() {
        return paths;
    }

}
