package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.Constraint;

/**
 * Created by moti on 5/9/2017.
 */
public class PushdownRelProp extends RelProp {
    //region Constructors
    public PushdownRelProp(String pushdownPropName) {
        this.pushdownPropName = pushdownPropName;
    }
    //endregion

    //region Properties
    public String getPushdownPropName() {
        return pushdownPropName;
    }
    //endregion

    //region Fields
    private String pushdownPropName;
    //endregion

    public static PushdownRelProp of(int eNum, String pushdownPropName, String pType, Constraint constraint){
        PushdownRelProp relProp = new PushdownRelProp(pushdownPropName);
        relProp.seteNum(eNum);
        relProp.setCon(constraint);
        relProp.setpType(pType);
        return relProp;
    }
}
