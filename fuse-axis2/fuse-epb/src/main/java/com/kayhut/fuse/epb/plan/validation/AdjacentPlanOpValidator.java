package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import org.apache.tinkerpop.gremlin.util.function.TriFunction;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Roman on 25/04/2017.
 */
public class AdjacentPlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region Constructors
    public AdjacentPlanOpValidator() {
        this.previousPlanOp = Optional.empty();
    }
    //endregion

    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {
        this.previousPlanOp = Optional.empty();
    }

    @Override
    public boolean isPlanOpValid(PlanOpBase planOp, AsgQuery query) {
        if (!(this.previousPlanOp.isPresent())) {
            return planOp instanceof EntityOp;
        }

        return false;
    }
    //endregion

    //region Private Methods
    //endregion

    //region Fields
    private Optional<PlanOpBase> previousPlanOp;
    //endregion
}
