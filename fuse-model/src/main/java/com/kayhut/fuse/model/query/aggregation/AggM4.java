package com.kayhut.fuse.model.query.aggregation;

/**
 * Created by User on 19/02/2017.
 */
public class AggM4 extends AggMBase {
    //region Properties
    public String[] getETag() {
        return this.eTag;
    }

    public void setETag(String[] value) {
        this.eTag = value;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String value) {
        this.tag = value;
    }
    //endregion

    //region Fields
    private String[] eTag;
    private String tag;
    //endregion
}
