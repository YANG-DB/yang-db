package com.kayhut.fuse.neo4j.cypher;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by elad on 19/03/2017.
 */
public class CypherRelationship extends CypherElement{

    @Override
    public CypherElement copy() {
        CypherRelationship rel = new CypherRelationship();
        rel.tag = tag;
        rel.label = label;
        rel.direction = direction;
        if(inlineProps != null) {
            rel.inlineProps = new HashMap<>();
            for (Map.Entry<String, String> entry :
                 inlineProps.entrySet()) {
                rel.inlineProps.put(entry.getKey(),entry.getValue());
            }
        }
        return rel;
    }

    public enum Direction{BOTH, LEFT, RIGHT};

    Direction direction;

    private CypherRelationship() {

    }

    public static CypherRelationship cypherRel() {
        return new CypherRelationship();
    }

    public CypherRelationship withTag(String relTag) {
        tag = relTag;
        return this;
    }

    public CypherRelationship withLabel(String relLabel) {
        label = relLabel;
        return this;
    }

    public CypherRelationship withProps(Map<String, String> props) {
        inlineProps = props;
        return this;
    }

    public CypherRelationship withDirection(Direction dir) {
        direction = dir;
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (direction == Direction.LEFT) {
            sb.append("<");
        }
        sb.append(String.format("-[%s%s%s]-", tag == null ? "" : tag,
                label == null ? "" : ":"+label.replace(" ", "_"),
                inlineProps == null ? "" : buildPropsStr()));
        if (direction == Direction.RIGHT) {
            sb.append(">");
        }
        return sb.toString();
    }

}
