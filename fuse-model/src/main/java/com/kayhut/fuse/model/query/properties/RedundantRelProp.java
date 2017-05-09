package com.kayhut.fuse.model.query.properties;

/**
 * Created by moti on 5/9/2017.
 */
public class RedundantRelProp extends RelProp {

    public RedundantRelProp(String redundantPropName) {
        this.redundantPropName = redundantPropName;
    }

    public String getRedundantPropName() {
        return redundantPropName;
    }

    private String redundantPropName;
}
