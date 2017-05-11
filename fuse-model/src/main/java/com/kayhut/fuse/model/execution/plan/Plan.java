package com.kayhut.fuse.model.execution.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kayhut.fuse.model.log.Trace;
import javaslang.Tuple2;

import java.util.List;
import java.util.logging.Level;

import static com.kayhut.fuse.model.Utils.pattern;
import static com.kayhut.fuse.model.Utils.simplePattern;

/**
 * Created by User on 22/02/2017.
 */
public class Plan extends CompositePlanOpBase implements Trace<String> {
    private Trace<String> trace = Trace.build(Plan.class.getSimpleName());

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

    //region Properties
    public Plan withOp(PlanOpBase op) {
        Plan newPlan = new Plan(this.getOps());
        newPlan.getOps().add(op);
        newPlan.trace = this.trace;
        return newPlan;
    }

    //endregion

    //endregion

    public String toPattern() {
        return toPattern(this);
    }

    public static String toSimplePattern(Plan plan) {
        return simplePattern(plan.getOps());
    }

    public static String toPattern(CompositePlanOpBase plan) {
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
    public List<Tuple2<String,String>> getLogs(Level level) {
        return trace.getLogs(level);
    }

    @Override
    public String who() {
        return trace.who();
    }

    public static boolean equals(Plan plan, Plan newPlan) {
        return toPattern(newPlan).compareTo(toPattern(plan))==0;
    }

    public static String diff(Plan plan, Plan newPlan) {
        return toPattern(newPlan).replace(toPattern(plan),"");
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
}


