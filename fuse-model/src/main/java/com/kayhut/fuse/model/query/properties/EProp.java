package com.kayhut.fuse.model.query.properties;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.Constraint;

/**
 * Created by benishue on 17/02/2017.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EProp extends BaseProp {
    //region Constructors
    public EProp() {
        super();
    }

    public EProp(int eNum, String pType, Constraint con) {
        super(eNum, pType, con);
    }
    //endregion


    public static EProp of(int pType, int eNum, Constraint con) {
        return of(Integer.toString(pType),eNum,con);
    }

    public static EProp of(String pType, int eNum, Constraint con) {
        EProp eProp = new EProp();
        eProp.setpType(pType);
        eProp.setCon(con);
        eProp.seteNum(eNum);
        return eProp;
    }

}
