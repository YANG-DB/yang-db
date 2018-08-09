package com.kayhut.fuse.model.query.properties.constraint;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ParameterizedConstraint extends Constraint {
    public ParameterizedConstraint() {}

    public ParameterizedConstraint(ConstraintOp op, NamedParameter parameter) {
        super(op,parameter);
    }

    @Override
    public NamedParameter getExpr() {
        return (NamedParameter) super.getExpr();
    }


    public static ParameterizedConstraint of(ConstraintOp op) {
        return of(op, null, "[]");
    }

    public static ParameterizedConstraint of(ConstraintOp op, NamedParameter exp) {
        return of(op, exp, "[]");
    }

    public static ParameterizedConstraint of(ConstraintOp op, NamedParameter exp, String iType) {
        ParameterizedConstraint constraint = new ParameterizedConstraint();
        constraint.setExpr(exp);
        constraint.setOp(op);
        constraint.setiType(iType);
        return constraint;
    }
}
