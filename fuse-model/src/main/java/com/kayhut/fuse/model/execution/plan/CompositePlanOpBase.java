package com.kayhut.fuse.model.execution.plan;

import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kayhut.fuse.model.Utils.fullPattern;

/**
 * Created by Roman on 24/04/2017.
 */
public abstract class CompositePlanOpBase extends PlanOpBase {
    //region Constructors
    private CompositePlanOpBase() {}

    public CompositePlanOpBase(Iterable<PlanOpBase> ops) {
        this.ops = Stream.ofAll(ops).toJavaList();
    }

    public CompositePlanOpBase(PlanOpBase...ops) {
        this(Stream.of(ops));
    }

    public CompositePlanOpBase(CompositePlanOpBase compositePlanOp) {
        this(compositePlanOp.getOps());
    }
    //endregion

    //region Properties
    public <TPlan extends CompositePlanOpBase> TPlan withOp(PlanOpBase op) {
        Plan newPlan = new Plan(this.getOps());
        newPlan.getOps().add(op);
        return (TPlan)newPlan;
    }

    public <TPlan extends CompositePlanOpBase> TPlan append(CompositePlanOpBase compositePlanOp) {
        Plan newPlan = new Plan(this.getOps());
        newPlan.getOps().addAll(compositePlanOp.getOps());
        return (TPlan)newPlan;
    }

    public List<PlanOpBase> getOps() {
        return this.ops;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + fullPattern(this.getOps()) + "]";
    }
    //endregion

    //region Fields
    private List<PlanOpBase> ops;
    //endregion
}
