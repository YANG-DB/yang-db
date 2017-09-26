package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.Constraint;

/**
 * Created by roman.margolis on 26/09/2017.
 */
public class RedundantSelectionRelProp extends RelProp {
    //region Constructors
    public RedundantSelectionRelProp(String redundantPropName) {
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

    public static RedundantRelProp of(int eNum, String redundantPropName, String pType){
        RedundantRelProp relProp = new RedundantRelProp(redundantPropName);
        relProp.seteNum(eNum);
        relProp.setpType(pType);
        return relProp;
    }
}
