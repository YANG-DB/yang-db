package com.kayhut.fuse.model.execution.plan.composite;

import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.PlanOp;

import static com.kayhut.fuse.model.Utils.*;

/**
 * Created by User on 22/02/2017.
 */
public class Plan extends CompositePlanOp implements IPlan {
    //region Constructors
    public Plan() {}

    public Plan(PlanOp... ops) {
        super(ops);
    }

    public Plan(Iterable<PlanOp> ops) {
        super(ops);
    }
    //endregion

    @Override
    public String toString() {
        return toFullPattern(this);
    }

    public String toPattern() {
        return toPattern(this);
    }

    public static String toSimplePattern(Plan plan) {
        return simplePattern(plan.getOps());
    }

    public static String toFullPattern(CompositePlanOp plan) {
        return fullPattern(plan.getOps());
    }

    public static String toPattern(CompositePlanOp plan) {
        return pattern(plan.getOps());
    }

    public static boolean contains(Plan plan,PlanOp op) {
        return plan.getOps().stream().anyMatch(p->p.equals(op));
    }

    public static boolean equals(Plan plan, Plan newPlan) {
        return toSimplePattern(newPlan).compareTo(toSimplePattern(plan))==0;
    }

    public static String diff(Plan plan, Plan newPlan) {
        return toSimplePattern(newPlan).replace(toSimplePattern(plan),"");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return equals((Plan)o,this);
    }

    @Override
    public int hashCode() {
        return toPattern().hashCode();
    }

    public static Plan clone(Plan plan) {
        return new Plan(plan.getOps());
    }
}