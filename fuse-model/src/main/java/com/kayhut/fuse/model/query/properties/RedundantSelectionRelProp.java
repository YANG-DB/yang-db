package com.kayhut.fuse.model.query.properties;

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

    public static RedundantSelectionRelProp of(int eNum, String redundantPropName, String pType){
        RedundantSelectionRelProp relProp = new RedundantSelectionRelProp(redundantPropName);
        relProp.seteNum(eNum);
        relProp.setpType(pType);
        return relProp;
    }
}
