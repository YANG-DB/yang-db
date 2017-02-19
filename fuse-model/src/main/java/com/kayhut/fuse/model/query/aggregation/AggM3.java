package com.kayhut.fuse.model.query.aggregation;

/**
 * Created by User on 19/02/2017.
 */
public class AggM3 extends AggMBase {
    //region Properties
    public String[] getETag() {
        return this.eTag;
    }

    public void setETag(String[] value) {
        this.eTag = value;
    }

    public AggLOp getAggOp() {
        return this.aggOp;
    }

    public void setAggOp(AggLOp aggOp) {
        this.aggOp = aggOp;
    }

    public int getPType() {
        return this.pType;
    }

    public void setpType(int value) {
        this.pType = value;
    }
    //endregion

    //region Fields
    private String[] eTag;
    private AggLOp aggOp;
    private int pType;
    //endregion
}
