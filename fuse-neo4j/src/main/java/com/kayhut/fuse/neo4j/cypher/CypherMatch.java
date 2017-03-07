package com.kayhut.fuse.neo4j.cypher;

import java.util.Map;

/**
 * Created by elad on 06/03/2017.
 */
public class CypherMatch {

    final String MATCH = "MATCH ";
    final String NODE_FORMAT = "(%s:%s)";
    final String NODE_WITH_PROP_FORMAT = "(%s:%s {%s})";
    final String PROP_NAME_VAL_FORMAT = "%s: %s,";
    final String RIGHT_REL_FORMAT = "-[%s:%s]->";
    final String LEFT_REL_FORMAT = "<-[%s:%s]-";
    final String BOTH_REL_FORMAT = "-[%s:%s]-";


    public enum Direction {RIGHT, LEFT, BOTH}

    private StringBuilder builder;

    public CypherMatch() {
        builder = new StringBuilder(MATCH);
    }

    public CypherMatch(String initialString) {
        builder = new StringBuilder(initialString);
    }

    public void appendNode(String tag, String label, Map<String, String> inlineProps) {
        if(inlineProps == null) {
            builder.append(String.format(NODE_FORMAT, tag == null ? "" : tag, label));
        } else {
            builder.append(String.format(NODE_WITH_PROP_FORMAT, tag, label, buildProps(inlineProps)));
        }
    }

    public void appendRelationship(String tag, String label, Map<String, String> inlineProps, Direction dir) {
        if(inlineProps == null) {
            switch (dir) {

               case RIGHT:
                   builder.append(String.format(RIGHT_REL_FORMAT, tag == null ? "" : tag, label));
                    break;
                case LEFT:
                    builder.append(String.format(LEFT_REL_FORMAT, tag == null ? "" : tag, label));
                    break;
                case BOTH:
                    builder.append(String.format(BOTH_REL_FORMAT, tag == null ? "" : tag, label));
                    break;
            }

        } else {

        }
    }

    public CypherMatch and(CypherMatch otherMatch) {
        CypherMatch newMatch = new CypherMatch(this.toString() + "\n" + otherMatch.toString());
        return newMatch;
    }

    private String buildProps(Map<String, String> props) {
        StringBuilder propsStr = new StringBuilder();

        for (String propName : props.keySet()) {
            propsStr.append(String.format(PROP_NAME_VAL_FORMAT,propName,props.get(propName)));
        }

        String finalStr = propsStr.toString();
        return finalStr.substring(0, finalStr.length() - 2);
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
