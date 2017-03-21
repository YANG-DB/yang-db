package com.kayhut.fuse.neo4j.cypher;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by User on 19/03/2017.
 */
public class CypherPath {

    String tag;
    List<CypherElement> pathElements = new LinkedList<>();

    private CypherPath(String pathName) {
        tag = pathName;
    }

    public static CypherPath cypherPath(String pathName) { return new CypherPath(pathName);}

    public CypherPath appendNode(CypherNode node) {
        pathElements.add(node);
        return this;
    }

    public CypherPath appendRelationship(CypherRelationship rel) {
        pathElements.add(rel);
        return this;
    }

    public CypherElement getElementFromEnd(int n) {
        if(pathElements.size() < n) {
            return null;
        }
        return pathElements.get(pathElements.size() - n);
    }

    public String toString() {
        if(pathElements.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(tag + " = ");
        pathElements.forEach(element -> sb.append(element.toString()));
        return sb.toString();
    }

    public CypherPath copy() {
        CypherPath cp = new CypherPath(tag);
        for(CypherElement ce: pathElements) {
            cp.pathElements.add(ce.copy());
        }
        return cp;
    }
}
