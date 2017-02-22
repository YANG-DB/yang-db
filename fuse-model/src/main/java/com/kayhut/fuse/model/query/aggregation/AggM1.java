package com.kayhut.fuse.model.query.aggregation;

/**
 * Created by User on 19/02/2017.
 */
public class AggM1 extends AggMBase {
    //region Properties
    public String[] getETag() {
        return this.eTag;
    }

    public void setETag(String[] value) {
        this.eTag = value;
    }

    public String[] getETag2() {
        return this.eTag2;
    }

    public void setETag2(String[] value) {
        this.eTag2 = value;
    }
    //endregion

    //region Fields
    private String[] eTag;
    private String[] eTag2;
    //endregion
}
