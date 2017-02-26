package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.query.Constraint;

/**
 * Created by User on 22/02/2017.
 */
public class AttachedPropertyFilterOp extends PlanOpBase {
    //region Constructor
    public AttachedPropertyFilterOp() {

    }

    public AttachedPropertyFilterOp(String propName, Constraint condition) {
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

    public Constraint getCondition() {
        return this.condition;
    }

    public void setCondition(Constraint value) {
        this.condition = value;
    }
    //endregion

    //region Fields
    private String propName;
    private Constraint condition;
    //endregion
}
