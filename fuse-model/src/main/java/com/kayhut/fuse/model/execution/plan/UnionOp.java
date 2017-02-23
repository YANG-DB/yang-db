package com.kayhut.fuse.model.execution.plan;

import java.util.List;

/**
 * Created by User on 22/02/2017.
 */
public class UnionOp {
    //region Constructors
    public UnionOp() {

    }

    public UnionOp(List<Plan> branches) {
        this.branches = branches;
    }
    //endregion

    //region Properties
    public List<Plan> getBranches() {
        return this.branches;
    }

    public void setBranches(List<Plan> branches) {
        this.branches = branches;
    }
    //endregion

    //region Fields
    private List<Plan> branches;
    //endregion
}
