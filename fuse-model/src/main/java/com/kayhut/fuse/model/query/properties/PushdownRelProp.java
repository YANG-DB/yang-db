package com.kayhut.fuse.model.query.properties;

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
}
