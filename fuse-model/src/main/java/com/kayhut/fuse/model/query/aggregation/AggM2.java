package com.kayhut.fuse.model.query.aggregation;

/**
 * Created by User on 19/02/2017.
 */
public class AggM2 extends AggMBase {
    //region Properties
    public String[] getETag() {
        return this.eTag;
    }

    public void setETag(String[] value) {
        this.eTag = value;
    }
    //endregion

    //region Fields
    private String[] eTag;
    //endregion
}
