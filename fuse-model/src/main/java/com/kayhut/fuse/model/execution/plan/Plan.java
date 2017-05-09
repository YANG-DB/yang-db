package com.kayhut.fuse.model.execution.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static com.kayhut.fuse.model.Utils.pattern;

/**
 * Created by User on 22/02/2017.
 */
public class Plan extends CompositePlanOpBase implements Trace<String>{
    private Trace<String> trace = Trace.build();

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
        newPlan.trace = this.trace;
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
        return toPattern(this);
    }

    public static String toPattern(Plan plan) {
        return pattern(plan.getOps());
    }

    public static boolean contains(Plan plan,PlanOpBase op) {
        return plan.getOps().stream().anyMatch(p->p.equals(op));
    }

    public static Plan compose(Plan plan,PlanOpBase op) {
        return plan.withOp(op);
    }


    @Override
    public void log(String event, Level level) {
        trace.log(event,level);
    }

    @Override
    public List<String> getLogs(Level level) {
        return trace.getLogs(level);
    }

    public static boolean equals(Plan plan, Plan newPlan) {
        return toPattern(newPlan).compareTo(toPattern(plan))==0;
    }

    public static String diff(Plan plan, Plan newPlan) {
        return toPattern(newPlan).replace(toPattern(plan),"");
    }
}


