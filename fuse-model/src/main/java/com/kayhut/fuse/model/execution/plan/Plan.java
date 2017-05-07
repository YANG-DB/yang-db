package com.kayhut.fuse.model.execution.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kayhut.fuse.model.Utils.pattern;

/**
 * Created by User on 22/02/2017.
 */
public class Plan extends CompositePlanOpBase{
    //region Constructors
    private Plan() {}

    public Plan(List<PlanOpBase> ops) {
        this.ops = new ArrayList<>(ops);
    }

    public Plan(PlanOpBase...ops) {
        this.ops = new ArrayList<>(Arrays.asList(ops));
    }

    @Override
    @JsonIgnore
    public int geteNum() {
        return 0;
    }
    //endregion

    //region Properties
    public Plan withOp(PlanOpBase op) {
        Plan newPlan = new Plan(this.getOps());
        newPlan.getOps().add(op);
        return newPlan;
    }

    public List<PlanOpBase> getOps() {
        return this.ops;
    }
    //endregion

    //region Fields
    private List<PlanOpBase> ops;
    //endregion

    public String toPattern() {
        return pattern(getOps());
    }

    public static boolean contains(Plan plan,PlanOpBase op) {
        return plan.getOps().stream().anyMatch(p->p.equals(op));
    }

    public static Plan compose(Plan plan,PlanOpBase op) {
        return plan.withOp(op);
    }
}


