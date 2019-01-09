package com.kayhut.fuse.assembly.knowledge.parser.model;

import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;
import javaslang.control.Option;

public enum TypeConstraint {
    EQ("=",ConstraintOp.eq),
    EXACT("=",ConstraintOp.eq),
    IN("IN",ConstraintOp.inSet),
    RANGE("",ConstraintOp.inRange),
    LT("<",ConstraintOp.lt),
    GT(">",ConstraintOp.gt),
    PLAIN("CONTAINS",ConstraintOp.like);

    private String val;
    private ConstraintOp op;

    TypeConstraint(String val, ConstraintOp op){
        this.val = val;
        this.op = op;
    }

    public static Constraint asConstraint(String key,Object value) {
        final Option<TypeConstraint> constraints = Stream.of(TypeConstraint.values()).find(v -> v.name().equals(key));
        if(constraints.isEmpty())
            return Constraint.of(ConstraintOp.contains,value);

        return Constraint.of(constraints.get().op,value);
    }
}
