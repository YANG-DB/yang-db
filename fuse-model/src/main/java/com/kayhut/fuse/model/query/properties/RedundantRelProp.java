package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.Constraint;

/**
 * Created by moti on 5/9/2017.
 */
public class RedundantRelProp extends RelProp {
    //region Constructors
    public RedundantRelProp(String redundantPropName) {
        this.redundantPropName = redundantPropName;
    }
    //endregion

    //region Properties
    public String getRedundantPropName() {
        return redundantPropName;
    }
    //endregion

    //region Fields
    private String redundantPropName;
    //endregion

    public static RedundantRelProp of(int eNum, String redundantPropName, String pType, Constraint constraint){
        RedundantRelProp relProp = new RedundantRelProp(redundantPropName);
        relProp.seteNum(eNum);
        relProp.setCon(constraint);
        relProp.setpType(pType);
        return relProp;
    }
}
