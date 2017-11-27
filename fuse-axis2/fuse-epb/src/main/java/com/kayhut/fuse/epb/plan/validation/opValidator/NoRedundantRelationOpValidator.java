package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.utils.ValidationContext;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.log.Trace;
import javaslang.Tuple2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static com.kayhut.fuse.model.execution.plan.composite.Plan.toPattern;

/**
 * Created by Roman on 30/04/2017.
 */
public class NoRedundantRelationOpValidator implements ChainedPlanValidator.PlanOpValidator{
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
    public ValidationContext isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (!(planOp instanceof RelationOp)) {
            return ValidationContext.OK;
        }

        if (!this.relationEnums.contains(((RelationOp) planOp).getAsgEbase().geteNum())){
            this.relationEnums.add(((RelationOp) planOp).getAsgEbase().geteNum());
            return ValidationContext.OK;
        }

        return new ValidationContext(false,"NoRedundant:Validation failed on:"+toPattern(compositePlanOp)+"<"+opIndex+">");
    }
    //endregion

    //region Fields
    private Set<Integer> relationEnums;
    //endregion
}
