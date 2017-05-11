package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.log.Trace;
import javaslang.Tuple2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static com.kayhut.fuse.model.execution.plan.Plan.toPattern;

/**
 * Created by Roman on 30/04/2017.
 */
public class RedundantGoToEntityOpValidator implements ChainedPlanValidator.PlanOpValidator {
    private Trace<String> trace = Trace.build(RedundantGoToEntityOpValidator.class.getSimpleName());


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

    //region Constructors
    public RedundantGoToEntityOpValidator() {
        this.entityEnums = new HashSet<>();
    }
    //endregion

    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {
        this.entityEnums.clear();
    }

    @Override
    public boolean isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex) {
        PlanOpBase planOp = compositePlanOp.getOps().get(opIndex);
        if (planOp instanceof GoToEntityOp) {
            if (!this.entityEnums.contains(planOp.geteNum())) {
                log("GoTo:Validation failed on:"+toPattern(compositePlanOp)+"<"+opIndex+">", Level.INFO);
                return false;
            }
        }

        if (planOp instanceof EntityOp) {
            this.entityEnums.add(planOp.geteNum());
        }

        return true;
    }
    //endregion

    //region Fields
    private Set<Integer> entityEnums;
    //endregion
}
