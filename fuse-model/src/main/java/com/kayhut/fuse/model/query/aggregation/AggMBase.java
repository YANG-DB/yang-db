package com.kayhut.fuse.model.query.aggregation;

import com.kayhut.fuse.model.query.Condition;

/**
 * Created by User on 19/02/2017.
 */
public abstract class AggMBase {
    //region Properties
    public String[] getPer() {
        return this.per;
    }

    public void setPer(String[] value) {
        this.per = value;
    }

    public int getN() {
        return this.n;
    }

    public void setN(int value) {
        this.n = value;
    }

    public AggMOp getOp() {
        return this.op;
    }

    public void setOp(AggMOp value) {
        this.op = value;
    }

    public int getB() {
        return this.b;
    }

    public void setB(int value) {
        this.b = value;
    }
    //endregion

    //region Fields
    private int n;
    private String[] per;
    private AggMOp op;
    private int b;
    //endregion
}
