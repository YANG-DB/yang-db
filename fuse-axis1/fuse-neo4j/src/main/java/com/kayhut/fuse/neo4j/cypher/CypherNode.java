package com.kayhut.fuse.neo4j.cypher;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by elad on 19/03/2017.
 */
public class CypherNode extends CypherElement{

    private CypherNode() {

    }

    public static CypherNode cypherNode() {return new CypherNode();}

    public CypherNode withTag(String nodeTag) {
        this.tag = nodeTag;
        return this;
    }

    public CypherNode withLabel(String nodeLabel) {
        this.label = nodeLabel;
        return this;
    }

    public CypherNode withProps(Map<String, String> props) {
        this.inlineProps = props;
        return this;
    }

    public String toString() {
        return String.format("(%s%s%s)", tag == null ? "" : tag,
                                          label == null ? "" : ":"+label,
                                          inlineProps == null ? "" : buildPropsStr());
    }

    @Override
    public CypherElement copy() {
        CypherNode node = new CypherNode();
        node.tag = tag;
        node.label = label;
        if(inlineProps != null) {
            node.inlineProps = new HashMap<>();
            for (Map.Entry<String, String> entry :
                    inlineProps.entrySet()) {
                node.inlineProps.put(entry.getKey(),entry.getValue());
            }
        }
        return node;
    }

}
