package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.model.validation.QueryValidation;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;

import java.util.HashSet;
import java.util.Set;

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
    public QueryValidation isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (!(planOp instanceof RelationOp)) {
            return QueryValidation.OK;
        }

        if (!this.relationEnums.contains(((RelationOp) planOp).getAsgEbase().geteNum())){
            this.relationEnums.add(((RelationOp) planOp).getAsgEbase().geteNum());
            return QueryValidation.OK;
        }

        return new QueryValidation(
                false,
                "NoRedundant:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
    }
    //endregion

    //region Fields
    private Set<Integer> relationEnums;
    //endregion
}
