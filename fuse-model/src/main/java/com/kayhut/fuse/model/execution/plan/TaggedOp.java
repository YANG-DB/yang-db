package com.kayhut.fuse.model.execution.plan;

/**
 * Created by User on 20/02/2017.
 */
public class TaggedOp extends PlanOpBase {
    //region Constructor
    public TaggedOp() {

    }

    public TaggedOp(String tag) {
        this.setTag(tag);
    }
    //endregion

    //region Properties
    public String getTag() {
        return this.tag;
    }

    public void setTag(String value) {
        this.tag = value;
    }
    //endregion

    //region Fields
    private String tag;
    //endregion
}
