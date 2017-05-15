package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.Constraint;

/**
 * Created by moti on 5/9/2017.
 */
public class PushdownRelProp extends RelProp {

    public PushdownRelProp(String pushdownPropName) {
        this.pushdownPropName = pushdownPropName;
    }

    public String getPushdownPropName() {
        return pushdownPropName;
    }

    private String pushdownPropName;

    public static PushdownRelProp of(String pushdownPropName, String pType, int num, Constraint constraint){
        PushdownRelProp relProp = new PushdownRelProp(pushdownPropName);
        relProp.setCon(constraint);
        relProp.setpType(pType);
        relProp.seteNum(num);
        return relProp;
    }
}
