package com.kayhut.fuse.model.query.entity;

import com.kayhut.fuse.model.query.EBase;

/**
 * Created by User on 27/02/2017.
 */
public abstract class EEntityBase extends EBase {
    //region Properties
    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }
    //endregion

    //region Fields
    private	String eTag;
    //endregion
}
