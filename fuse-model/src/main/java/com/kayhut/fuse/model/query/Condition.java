package com.kayhut.fuse.model.query;

/**
 * Created by benishue on 17/02/2017.
 */
public class Condition {
    //region Properties
    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object r) {
        this.value = r;
    }
    //endregion

    //region Fields
    private String op;
    private Object value;
    //endregion

}
