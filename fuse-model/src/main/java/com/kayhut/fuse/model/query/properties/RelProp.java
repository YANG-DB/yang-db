package com.kayhut.fuse.model.query.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RelProp extends BaseProp {
    //region Constructors
    public RelProp() {
        super();
    }

    public RelProp(int eNum, String pType, Constraint con, int b) {
        super(eNum, pType, con);
        this.b = b;
    }
    //endregion

    //region Properties
    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
    //endregion

    //region Fields
    private int b;
    //endregion

    public static RelProp of(int pType,int eNum,Constraint con) {
        RelProp eProp = new RelProp();
        eProp.setpType(Integer.valueOf(pType).toString());
        eProp.setCon(con);
        eProp.seteNum(eNum);
        return eProp;
    }

    public static RelProp of(String pType,int eNum,Constraint con) {
        RelProp eProp = new RelProp();
        eProp.setpType(pType);
        eProp.setCon(con);
        eProp.seteNum(eNum);
        return eProp;
    }

}
