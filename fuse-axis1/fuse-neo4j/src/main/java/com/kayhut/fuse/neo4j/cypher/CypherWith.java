package com.kayhut.fuse.neo4j.cypher;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Elad on 4/2/2017.
 */
public class CypherWith {

    private List<CypherReturnElement> elements = new LinkedList<>();

    private CypherWith() {

    }

    public static CypherWith cypherWith() { return new CypherWith(); }

    public CypherWith withElement(CypherReturnElement element) {
        elements.add(element);
        return this;
    }

    public CypherWith copy() {
        CypherWith newRet = new CypherWith();
        for (CypherReturnElement ret :
                elements) {
            newRet.elements.add(ret.copy());
        }
        return newRet;
    }

    public List<CypherReturnElement> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        if(elements.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\nWITH ");
        elements.forEach(e -> sb.append(e.toString() + ","));
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

}
