package com.kayhut.fuse.model.query.properties;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.EBase;

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

    public Constraint getCon() {
        return con;
    }

    public void setCon(Constraint con) {
        this.con = con;
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
    private Constraint con;
    private String f;
    //endregion

}
