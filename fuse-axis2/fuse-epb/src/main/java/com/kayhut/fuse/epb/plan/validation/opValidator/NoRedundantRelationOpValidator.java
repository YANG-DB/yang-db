package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.CompositePlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
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
public class NoRedundantRelationOpValidator implements ChainedPlanValidator.PlanOpValidator{
    private Trace<String> trace = Trace.build(NoRedundantRelationOpValidator.class.getSimpleName());

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
    public NoRedundantRelationOpValidator() {
        this.relationEnums = new HashSet<>();
    }
    //endregion

    //region ChainedPlanValidator Implementation
    @Override
    public void reset() {
        this.relationEnums.clear();
    }

    @Override
    public ValidationContext isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex) {
        PlanOpBase planOp = compositePlanOp.getOps().get(opIndex);
        if (!(planOp instanceof RelationOp)) {
            return ValidationContext.OK;
        }

        if (!this.relationEnums.contains(planOp.geteNum())){
            this.relationEnums.add(planOp.geteNum());
            return ValidationContext.OK;
        }

        log("NoRedundant:Validation failed on:"+toPattern(compositePlanOp)+"<"+opIndex+">", Level.INFO);
        return new ValidationContext(false,"NoRedundant:Validation failed on:"+toPattern(compositePlanOp)+"<"+opIndex+">");
    }
    //endregion

    //region Fields
    private Set<Integer> relationEnums;
    //endregion
}
