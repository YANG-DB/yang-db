package com.kayhut.fuse.neo4j.cypher;

import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;

/**
 * Created by Elad on 26/03/2017.
 */
public interface CypherOps {
    static String getOp(ConstraintOp op) {
        switch (op.name()) {
            case "lt":
                return "<";
            case "eq":
                return "=";
            case "le":
                return "<=";
            case "gt":
                return ">";
            case "ge":
                return ">=";
            case "empty":
                return "=";
            case "not_empty":
                return "<>";
            default:
                return " <> ";
        }
    }
}
