package com.kayhut.fuse.model.execution.plan;

/**
 * Created by User on 20/02/2017.
 */
public abstract class PlanOpBase {

    public int geteNum() {
        return eNum;
    }

    public void seteNum(int eNum) {
        this.eNum = eNum;
    }

    //region fields
    private int eNum;
    //endregion
}
