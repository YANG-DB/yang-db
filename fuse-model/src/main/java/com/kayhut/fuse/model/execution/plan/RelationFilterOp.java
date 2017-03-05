package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.query.properties.RelProp;

/**
 * Created by User on 22/02/2017.
 */
public class RelationFilterOp extends PlanOpBase {
    //region Constructors
    public RelationFilterOp() {

    }

    public RelationFilterOp(RelProp relProp) {
        this.relProp = relProp;
    }
    //endregion

    //region Properties
    public RelProp getRelProp() {
        return this.relProp;
    }

    public void setRelProp(RelProp value) {
        this.relProp = value;
    }
    //endregion

    //region Fields
    private RelProp relProp;
    //endregion
}
