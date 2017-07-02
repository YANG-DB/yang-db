package com.kayhut.fuse.model.query.aggregation;

/**
 * Created by User on 19/02/2017.
 */
public class AggL4 extends AggLBase {
    //region Properties
    public AggLOp getAggLOp() {
        return this.aggOp;
    }

    public void setAggLOp(AggLOp value) {
        this.aggOp = value;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String value) {
        this.tag = value;
    }
    //endregion

    //region Fields
    private AggLOp aggOp;
    private String tag;
    //endregion
}
