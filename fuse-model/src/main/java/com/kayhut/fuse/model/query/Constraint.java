package com.kayhut.fuse.model.query;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Constraint {

    //region Ctrs
    public Constraint() {
    }

    public Constraint(ConstraintOp op, Object expr) {
        this.op = op;
        this.expr = expr;
    }

    public Constraint(ConstraintOp op, Object expr, String iType) {
        this.op = op;
        this.expr = expr;
        this.iType = iType;
    }
    //endregion

    //region Properties
    public ConstraintOp getOp() {
        return op;
    }

    public void setOp(ConstraintOp op) {
        this.op = op;
    }

    public Object getExpr() {
        return expr;
    }

    public void setExpr(Object expr) {
        this.expr = expr;
    }

    public String getiType() {
        return iType;
    }

    public void setiType(String iType) {
        this.iType = iType;
    }
    //endregion

    //region Fields
    private ConstraintOp op;
    private Object expr;
    //default - inclusive
    private String iType = "[]";
    //endregion

    public static Constraint of(ConstraintOp op, Object exp) {
        return of(op,exp,"[]");
    }

    public static Constraint of(ConstraintOp op,Object exp, String iType) {
        Constraint constraint = new Constraint();
        constraint.setExpr(exp);
        constraint.setOp(op);
        constraint.setiType(iType);
        return constraint;
    }

}
