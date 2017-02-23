package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.query.Condition;

/**
 * Created by User on 22/02/2017.
 */
public class AttachedPropertyFilterOp extends PlanOpBase {
    //region Constructor
    public AttachedPropertyFilterOp() {

    }

    public AttachedPropertyFilterOp(String propName, Condition condition) {
        this.propName = propName;
        this.condition = condition;
    }
    //endregion

    //region Properties
    public String getPropName() {
        return this.propName;
    }

    public void setPropName(String value) {
        this.propName = value;
    }

    public Condition getCondition() {
        return this.condition;
    }

    public void setCondition(Condition value) {
        this.condition = value;
    }
    //endregion

    //region Fields
    private String propName;
    private Condition condition;
    //endregion
}
