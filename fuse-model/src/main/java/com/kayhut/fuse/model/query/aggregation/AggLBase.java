package com.kayhut.fuse.model.query.aggregation;

import com.kayhut.fuse.model.query.Condition;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by User on 19/02/2017.
 */
public abstract class AggLBase extends EBase {
    //region Properties
    public String[] getPer() {
        return this.per;
    }

    public void setPer(String[] value) {
        this.per = value;
    }

    public String getATag() {
        return this.aTag;
    }

    public void setATag(String value) {
        this.aTag = value;
    }

    public Condition getCond() {
        return this.cond;
    }

    public void setCond(Condition value) {
        this.cond = value;
    }

    public int getB() {
        return this.b;
    }

    public void setB(int value) {
        this.b = value;
    }
    //endregion

    //region Fields
    private String aTag;
    private String[] per;
    private Condition cond;
    private int b;
    //endregion
}
