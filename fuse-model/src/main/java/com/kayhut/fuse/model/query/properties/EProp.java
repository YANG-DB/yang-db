package com.kayhut.fuse.model.query.properties;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.List;

/**
 * Created by benishue on 17/02/2017.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EProp extends EBase {
    //region Constructors
    public EProp() {
        super();
    }

    public EProp(int eNum, String pType, Constraint con) {
        super(eNum);
        this.pType = pType;
        this.con = con;
    }
    //endregion

    //region Properties
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
    //endregion

    //region Fields
    private String pType;
    private String pTag;
    private Constraint con;
    private String f;
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EProp eProp = (EProp) o;

        if (!pType.equals(eProp.pType)) return false;
        if (!pTag.equals(eProp.pTag)) return false;
        if (!con.equals(eProp.con)) return false;
        return f != null ? f.equals(eProp.f) : eProp.f == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + pType.hashCode();
        result = 31 * result + pTag.hashCode();
        result = 31 * result + con.hashCode();
        result = 31 * result + (f != null ? f.hashCode() : 0);
        return result;
    }
    //endregion

    public static EProp of(String pType, int eNum, Constraint con) {
        EProp eProp = new EProp();
        eProp.setpType(pType);
        eProp.setCon(con);
        eProp.seteNum(eNum);
        return eProp;
    }
}
