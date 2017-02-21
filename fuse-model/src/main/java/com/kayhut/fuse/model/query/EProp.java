package com.kayhut.fuse.model.query;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by benishue on 17/02/2017.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EProp extends EBase {

    public int getpType() {
        return pType;
    }

    public void setpType(int pType) {
        this.pType = pType;
    }

    public String getpTag() {
        return pTag;
    }

    public void setpTag(String pTag) {
        this.pTag = pTag;
    }

    public Condition getCond() {
        return cond;
    }

    public void setCond(Condition cond) {
        this.cond = cond;
    }

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    //region Fields
    private int pType;
    private String pTag;
    private Condition cond;
    private String f;
    //endregion

}
