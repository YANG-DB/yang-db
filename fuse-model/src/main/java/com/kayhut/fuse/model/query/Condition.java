package com.kayhut.fuse.model.query;

/**
 * Created by benishue on 17/02/2017.
 */
public class Condition {

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    //region Fields
    private String op;
    private Object value;
    //endregion

}
