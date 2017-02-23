package com.kayhut.fuse.model.query.aggregation;

import com.kayhut.fuse.model.query.Condition;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by User on 22/02/2017.
 */
public class AggBase extends EBase {
    //region Properties
    public String[] getPer() {
        return this.per;
    }

    public void setPer(String[] value) {
        this.per = value;
    }

    public int getB() {
        return this.b;
    }

    public void setB(int value) {
        this.b = value;
    }
    //endregion

    //region Fields
    private String[] per;
    private int b;
    //endregion
}
