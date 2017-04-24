package com.kayhut.fuse.model.execution.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 22/02/2017.
 */
public class Plan extends CompositePlanOpBase{
    //region Constructors
    private Plan() {}

    public Plan(List<PlanOpBase> ops) {
        super(ops);
    }

    public Plan(PlanOpBase...ops) {
        super(ops);
    }

    @Override
    @JsonIgnore
    public int geteNum() {
        return 0;
    }
    //endregion
}
