package com.kayhut.fuse.neo4j.cypher;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by elad on 06/03/2017.
 */
public class CypherReturn {

    private List<CypherReturnElement> elements = new LinkedList<>();

    private CypherReturn() {

    }

    public static CypherReturn cypherReturn() {return new CypherReturn();}

    public CypherReturn withElement(CypherReturnElement element) {
        elements.add(element);
        return this;
    }

    public CypherReturn copy() {
        CypherReturn newRet = new CypherReturn();
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
        sb.append("\nRETURN ");
        elements.forEach(e -> sb.append(e.toString() + ","));
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

}
