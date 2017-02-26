package com.kayhut.fuse.model.query;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Constraint {

    //region Properties
    public CostraintOp getOp() {
        return op;
    }

    public void setOp(CostraintOp op) {
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
    private CostraintOp op;
    private Object expr;
    private String iType;
    //endregion

}
