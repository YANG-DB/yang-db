package com.kayhut.fuse.model.query;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Condition {
    //region Properties
    public ConditionOp getOp() {
        return op;
    }

    public void setOp(ConditionOp op) {
        this.op = op;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    //endregion

    //region Fields
    private ConditionOp op;
    private Object value;
    //endregion

}
