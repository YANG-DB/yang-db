package com.kayhut.fuse.model.query.aggregation;

import com.kayhut.fuse.model.query.Constraint;
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

    public Constraint getCon() {
        return this.con;
    }

    public void setCon(Constraint value) {
        this.con = value;
    }
    //endregion

    //region Fields
    private String aTag;
    private Constraint con;
    //endregion
}
