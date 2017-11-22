package com.kayhut.fuse.model.execution.plan;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 23/02/2017.
 */
public class OptionalOp extends CompositePlanOpBase {
    //region Constructors
    private OptionalOp() {}

    public OptionalOp(List<PlanOpBase> ops) {
        super(ops);
    }

    public OptionalOp(PlanOpBase...ops) {
        super(ops);
    }

    public OptionalOp(CompositePlanOpBase compositePlanOp) {
        super(compositePlanOp);
    }
    //endregion

    @Override
    public int geteNum() {
        throw new NotImplementedException();
    }
    //endregion
}
