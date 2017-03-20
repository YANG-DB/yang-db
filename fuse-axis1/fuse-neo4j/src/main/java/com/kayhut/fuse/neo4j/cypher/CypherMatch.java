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
        paths.values().forEach(p -> sb.append(String.format("MATCH %s,\n",p)));
        sb.deleteCharAt(sb.lastIndexOf("\n"));
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    public CypherMatch copy() {
        CypherMatch newMatch = new CypherMatch();
        newMatch.paths = paths;
        return newMatch;
    }

    public Map<String, CypherPath> getPaths() {
        return paths;
    }

}
