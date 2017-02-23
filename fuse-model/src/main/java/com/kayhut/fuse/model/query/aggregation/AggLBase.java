package com.kayhut.fuse.model.query.aggregation;

import com.kayhut.fuse.model.query.Condition;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by User on 19/02/2017.
 */
public abstract class AggLBase extends EBase {
    //region Properties
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
    //endregion

    //region Fields
    private String aTag;
    private Condition cond;
    //endregion
}
