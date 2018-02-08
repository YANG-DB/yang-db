package com.kayhut.fuse.model.query.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by moti on 5/17/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BaseProp extends EBase{
    public BaseProp() {

    }

    public BaseProp(int eNum, String pType, Constraint con) {
        super(eNum);
        this.pType = pType;
        this.con = con;
    }

    public String getpType() {
        return pType;
    }

    public void setpType(String pType) {
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
    private String pType;
    private String pTag;
    private Constraint con;
    private String f;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseProp eProp = (BaseProp) o;

        if (pType == null) {
            if (eProp.pType != null) {
                return false;
            }
        } else {
            if (!pType.equals(eProp.pType)) {
                return false;
            }
        }

        if (pTag == null) {
            if (eProp.pTag != null) {
                return false;
            }
        } else {
            if (!pTag.equals(eProp.pTag)) {
                return false;
            }
        }

        if (con == null) {
            if (eProp.con != null) {
                return false;
            }
        } else {
            if (!con.equals(eProp.con)) {
                return false;
            }
        }

        return f != null ? f.equals(eProp.f) : eProp.f == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (pType!=null ? pType.hashCode() : 0);
        result = 31 * result + (pTag!=null ? pTag.hashCode() : 0);
        result = 31 * result + (con!=null ? con.hashCode() : 0);
        result = 31 * result + (f != null ? f.hashCode() : 0);
        return result;
    }

}
