package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.projection.Projection;

/**
 * Created by roman.margolis on 26/09/2017.
 */
public class RedundantSelectionRelProp extends RelProp {
    //region Constructors
    public RedundantSelectionRelProp() {
        super();
    }

    public RedundantSelectionRelProp(int eNum, String pType, String redundantPropName, Projection proj, int b) {
        super(eNum, pType, proj, b);
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

    public static RedundantSelectionRelProp of(int eNum, String pType, String redundantPropName, Projection proj){
        RedundantSelectionRelProp relProp = new RedundantSelectionRelProp(eNum, pType, redundantPropName, proj, 0);
        relProp.seteNum(eNum);
        relProp.setpType(pType);
        return relProp;
    }
}
