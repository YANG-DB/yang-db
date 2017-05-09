package com.kayhut.fuse.model.query;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Constraint {

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
    private String iType;
    //endregion

    public static Constraint of(ConstraintOp op,String exp) {
        Constraint constraint = new Constraint();
        constraint.setExpr(exp);
        constraint.setOp(op);
        return constraint;
    }


}
