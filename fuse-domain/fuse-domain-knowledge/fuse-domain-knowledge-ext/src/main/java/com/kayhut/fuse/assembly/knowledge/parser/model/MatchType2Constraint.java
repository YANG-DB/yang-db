package com.kayhut.fuse.assembly.knowledge.parser.model;

import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;
import javaslang.control.Option;

public enum MatchType2Constraint {
    EQ("=",ConstraintOp.like),
    EXACT("=",ConstraintOp.eq),
    IN("IN",ConstraintOp.inSet),
    RANGE("",ConstraintOp.inRange),
    LT("<",ConstraintOp.lt),
    GT(">",ConstraintOp.gt),
    PLAIN("CONTAINS",ConstraintOp.contains);

    private String val;
    private ConstraintOp op;

    MatchType2Constraint(String val,ConstraintOp op){
        this.val = val;
        this.op = op;
    }

    public static Constraint asConstraint(String key,Object value) {
        final Option<MatchType2Constraint> constraints = Stream.of(MatchType2Constraint.values()).find(v -> v.name().equals(key));
        if(constraints.isEmpty())
            return Constraint.of(ConstraintOp.contains,value);

        return Constraint.of(constraints.get().op,value);
    }
}
