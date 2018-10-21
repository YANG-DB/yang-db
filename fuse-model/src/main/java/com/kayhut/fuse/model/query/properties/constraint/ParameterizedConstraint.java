package com.kayhut.fuse.model.query.properties.constraint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "OptionalUnaryParameterizedConstraint", value = OptionalUnaryParameterizedConstraint.class)})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterizedConstraint extends Constraint {
    public ParameterizedConstraint() {}

    public ParameterizedConstraint(ConstraintOp op, NamedParameter parameter) {
        super(op,parameter);
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
