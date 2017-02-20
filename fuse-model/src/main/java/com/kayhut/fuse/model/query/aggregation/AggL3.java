package com.kayhut.fuse.model.query.aggregation;

/**
 * Created by User on 19/02/2017.
 */
public class AggL3 extends AggLBase {
    //region Properties
    public AggLOp getAggOp() {
        return this.aggOp;
    }

    public void setAggOp(AggLOp value) {
        this.aggOp = value;
    }

    public int getPType() {
        return this.pType;
    }

    public void setPType(int value) {
        this.pType = value;
    }
    //endregion

    //region Fields
    private AggLOp aggOp;
    private int pType;
    //endregion
}
